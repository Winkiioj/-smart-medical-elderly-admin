package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.ImportLog;
import com.swjtu.smec.mapper.ImportLogMapper;
import com.swjtu.smec.service.ImportLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ImportLogServiceImpl extends ServiceImpl<ImportLogMapper, ImportLog>
        implements ImportLogService {
}
