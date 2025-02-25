package com.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.JianchaEntity;
import com.entity.YonghuEntity;
import com.entity.view.JianchaView;
import com.service.DictionaryService;
import com.service.JianchaService;
import com.service.TokenService;
import com.service.YonghuService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 检查
 * 后端接口
 * @author
 * @email
 * @date 2021-04-30
*/
@RestController
@Controller
@RequestMapping("/jiancha")
public class JianchaController {
    private static final Logger logger = LoggerFactory.getLogger(JianchaController.class);

    @Autowired
    private JianchaService jianchaService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;



    //级联表service
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
     
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isNotEmpty(role) && "用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        params.put("orderBy","id");
        PageUtils page = jianchaService.queryPage(params);

        //字典表数据转换
        List<JianchaView> list =(List<JianchaView>)page.getList();
        for(JianchaView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JianchaEntity jiancha = jianchaService.selectById(id);
        if(jiancha !=null){
            //entity转view
            JianchaView view = new JianchaView();
            BeanUtils.copyProperties( jiancha , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(jiancha.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody JianchaEntity jiancha, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,jiancha:{}",this.getClass().getName(),jiancha.toString());
        Wrapper<JianchaEntity> queryWrapper = new EntityWrapper<JianchaEntity>()
            .eq("yonghu_id", jiancha.getYonghuId())
            .eq("jiancha_name", jiancha.getJianchaName())
            .eq("jiancha_types", jiancha.getJianchaTypes())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JianchaEntity jianchaEntity = jianchaService.selectOne(queryWrapper);
        if(jianchaEntity==null){
            jiancha.setInsertTime(new Date());
            jiancha.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      jiancha.set
        //  }
            jianchaService.insert(jiancha);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody JianchaEntity jiancha, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,jiancha:{}",this.getClass().getName(),jiancha.toString());
        //根据字段查询是否有相同数据
        Wrapper<JianchaEntity> queryWrapper = new EntityWrapper<JianchaEntity>()
            .notIn("id",jiancha.getId())
            .andNew()
            .eq("yonghu_id", jiancha.getYonghuId())
            .eq("jiancha_name", jiancha.getJianchaName())
            .eq("jiancha_types", jiancha.getJianchaTypes())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JianchaEntity jianchaEntity = jianchaService.selectOne(queryWrapper);
        if(jianchaEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      jiancha.set
            //  }
            jianchaService.updateById(jiancha);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        jianchaService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }






}

