package com.memorymain.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.memorymain.config.MemoryConfig;
import com.memorymain.util.FileUtils;
import com.memorymain.util.R;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Paths;

@RestController
@RequestMapping("/file")
public class FileController {
    @PostMapping("/upload")
    public R<?> upload(@RequestParam(value = "file",required = false) MultipartFile file,
                       @RequestParam(value = "path") String path){
        try
        {
            // 上传文件路径
            String filePath = MemoryConfig.getProfile()+path;
            File desc = new File(filePath);
            if (!desc.exists())
            {
                if (!desc.getParentFile().exists())
                {
                    desc.getParentFile().mkdirs();
                }
            }
            String absPath = desc.getAbsolutePath();
            file.transferTo(Paths.get(absPath));
            return R.ok(filePath);

        }
        catch (Exception e)
        {
            return R.fail(e.getMessage());
        }
    }
    @SaIgnore
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            String realFileName = FileUtils.getName(fileName);
            String filePath = MemoryConfig.getProfile() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("下载文件失败:" + e);
        }
    }

    @DeleteMapping("deleteFile")
    public R<?> fileDownload(String fileName){
        String filePath = MemoryConfig.getProfile() + fileName;
        FileUtils.deleteFile(filePath);
        return R.ok();
    }
}
