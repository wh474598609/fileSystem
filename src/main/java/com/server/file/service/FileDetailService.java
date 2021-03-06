package com.server.file.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.server.file.DTO.FileDetailDTO;
import com.server.file.dao.FileDetailMapper;
import com.server.file.model.FileDetail;
import com.server.file.model.FileDetailExample;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Auther: Leo
 * @Date: 2019/1/12
 * @Description:
 */
@Service
public class FileDetailService {

    private final static Logger logger = LoggerFactory.getLogger(FileDetailService.class);

    @Autowired
    private FileDetailMapper fileDetailMapper;

    public PageInfo getFileList(int currentPage, int pageSize) {
        FileDetailExample example = new FileDetailExample();
        PageHelper.startPage(currentPage, 10);
        List<FileDetail> list = fileDetailMapper.selectByExample(example);
        List<FileDetailDTO> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (FileDetail detail : list) {
                FileDetailDTO dto = new FileDetailDTO();
                BeanUtils.copyProperties(detail, dto);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dto.setCreateTime(sdf.format(detail.getCreateTime()));
                result.add(dto);
            }
        }
        PageInfo<FileDetailDTO> pageInfo = new PageInfo<>(result);
        return pageInfo;
    }

    public void deleteFile(String id) {
        fileDetailMapper.deleteByPrimaryKey(id);
    }

    public void addFile(MultipartFile file) throws Exception {// 获取文件名
        String fileName = file.getOriginalFilename();
        logger.info("上传的文件名为：" + fileName);

        // 获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        logger.info("上传的后缀名为：" + suffixName);

        // 文件上传路径
        String filePath = "/static/file/";

        // 解决中文问题，liunx下中文路径，图片显示问题
         fileName = UUID.randomUUID() + suffixName;

        File dest = new File(filePath + fileName);

        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);
        FileDetail detail = new FileDetail();
        detail.setId(UUID.randomUUID().toString());
        detail.setCount(0);
        detail.setCreateTime(new Date());
        detail.setFileName(file.getOriginalFilename());
        detail.setFileUrl(filePath + fileName);
        fileDetailMapper.insertSelective(detail);
    }

    public FileDetail getFileById(String id){
        FileDetail detail = fileDetailMapper.selectByPrimaryKey(id);
        return detail;
    }

    public void updateDownloadCount(FileDetail detail){
        detail.setCount(detail.getCount()+1);
        fileDetailMapper.updateByPrimaryKeySelective(detail);
    }

}
