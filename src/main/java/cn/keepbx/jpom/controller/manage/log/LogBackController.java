package cn.keepbx.jpom.controller.manage.log;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.JsonMessage;
import cn.keepbx.jpom.common.BaseController;
import cn.keepbx.jpom.common.commander.AbstractCommander;
import cn.keepbx.jpom.common.interceptor.ProjectPermission;
import cn.keepbx.jpom.controller.manage.file.ProjectFileControl;
import cn.keepbx.jpom.model.ProjectInfoModel;
import cn.keepbx.jpom.service.manage.ProjectInfoService;
import com.alibaba.fastjson.JSONArray;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * 控制台日志备份管理
 *
 * @author jiangzeyin
 * @date 2019/3/7
 */
@Controller
@RequestMapping(value = "/manage/log")
public class LogBackController extends BaseController {
    @Resource
    private ProjectInfoService projectInfoService;

    @RequestMapping(value = "export.html", method = RequestMethod.GET)
    @ResponseBody
    public String export(String id) {
        ProjectInfoModel pim = projectInfoService.getItem(id);
        File file = new File(pim.getLog());
        if (!file.exists()) {
            return JsonMessage.getString(400, "没有日志文件:" + file.getPath());
        }
        HttpServletResponse response = getResponse();
        ServletUtil.write(response, file);
        return JsonMessage.getString(200, "");
    }

    @RequestMapping(value = "logBack", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String console(String id) {
        // 查询项目路径
        ProjectInfoModel pim = projectInfoService.getItem(id);
        if (pim != null) {
            File logBack = pim.getLogBack();
            if (logBack.exists() && logBack.isDirectory()) {
                File[] filesAll = logBack.listFiles();
                if (filesAll != null) {
                    JSONArray jsonArray = ProjectFileControl.parseInfo(filesAll, true);
                    setAttribute("array", jsonArray);
                }
            }
            setAttribute("id", pim.getId());
        }
        return "manage/logBack";
    }

    @RequestMapping(value = "logBack_download", method = RequestMethod.GET)
    @ResponseBody
    public String download(String id, String key) {
        key = pathSafe(key);
        if (StrUtil.isEmpty(key)) {
            return JsonMessage.getString(405, "非法操作");
        }
        try {
            ProjectInfoModel pim = projectInfoService.getItem(id);
            File logBack = pim.getLogBack();
            if (logBack.exists() && logBack.isDirectory()) {
                logBack = FileUtil.file(logBack, key);
                ServletUtil.write(getResponse(), logBack);
            } else {
                return "没有对应文件";
            }
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error("下载文件异常", e);
        }
        return "下载失败。请刷新页面后重试";
    }


    @RequestMapping(value = "logBack_delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ProjectPermission(checkDelete = true)
    public String clear(String name) {
        name = pathSafe(name);
        if (StrUtil.isEmpty(name)) {
            return JsonMessage.getString(405, "非法操作:" + name);
        }
        ProjectInfoModel pim = getProjectInfoModel();
        File logBack = pim.getLogBack();
        if (logBack.exists() && logBack.isDirectory()) {
            logBack = FileUtil.file(logBack, name);
            if (logBack.exists()) {
                FileUtil.del(logBack);
                return JsonMessage.getString(200, "删除成功");
            }
            return JsonMessage.getString(500, "没有对应文件");
        } else {
            return JsonMessage.getString(500, "没有对应文件夹");
        }
    }

    @RequestMapping(value = "logSize", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String logSize(String id) {
        String info = projectInfoService.getLogSize(id);
        if (info != null) {
            return JsonMessage.getString(200, "ok", info);
        }
        return JsonMessage.getString(500, "获取日志大小失败");
    }


    @RequestMapping(value = "resetLog", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String resetLog(String id) {
        ProjectInfoModel pim;
        try {
            pim = projectInfoService.getItem(id);
            String msg = AbstractCommander.getInstance().backLog(pim);
            if (msg.contains("ok")) {
                return JsonMessage.getString(200, "重置成功");
            }
            return JsonMessage.getString(201, "重置失败：" + msg);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
            return JsonMessage.getString(500, "重置日志失败");
        }
    }
}