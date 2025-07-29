package com.cywedding.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cywedding.dto.QRGroup;
import com.cywedding.dto.QRUser;
import com.cywedding.mapper.QRGroupMapper;
import com.cywedding.mapper.QRUserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QRGroupService {
    private static final Logger logger = LoggerFactory.getLogger(QRGroupService.class);

    private final QRGroupMapper groupMapper;
    private final QRUserMapper userMapper;

    public void createQR(String groupName, String prefix, Integer count) {
        QRGroup group = groupMapper.fetchQRGroup(groupName);

        if (group == null) {
            group = new QRGroup();
            group.setGroupName(groupName);

            groupMapper.createGroup(group);
        } else {
            userMapper.resetUserList(group.getGroupId());
        }
        group.setPrefix(prefix);
        group.setCount(count);

        createUserList(group);
    }

    public void createUserList(QRGroup group) {
        List<QRUser> userList = IntStream.rangeClosed(1, group.getCount())
            .mapToObj(i -> {
                QRUser user = new QRUser();
                user.setGroupId(group.getGroupId());
                user.setQrCode(group.getPrefix() + String.format("%03d", i));
                
                return user;
            })
            .collect(Collectors.toList());

        logger.info("생성된 QRUser 목록: {}", userList);
        userMapper.createUserList(userList);
    }
}
