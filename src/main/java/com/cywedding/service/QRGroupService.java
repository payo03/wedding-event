package com.cywedding.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cywedding.common.DMLType;
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

    public QRGroup fetchQRGroup(String groupName) {
        return groupMapper.fetchQRGroup(groupName);
    }

    @Transactional
    public void createQR(Map<String, String> infoMap) {
        String groupName = infoMap.get("groupName");
        String prefix = infoMap.get("prefix");
        String plan = infoMap.get("plan");
        Integer count = Integer.parseInt(infoMap.get("count"));
        Integer maxUploads = Integer.parseInt(infoMap.get("maxUploads"));
        Integer maxVotes = Integer.parseInt(infoMap.get("maxVotes"));
        Boolean isAutoNotice = Boolean.parseBoolean(infoMap.get("autoNotice"));

        QRGroup group = groupMapper.fetchQRGroup(groupName);

        if (group == null) {
            group = new QRGroup();
            group.setGroupName(groupName);
            group.setPlan(plan);

            groupMapper.createGroup(group);
        } else {
            userMapper.resetUserList(group.getGroupId());
        }
        group.setMaxUploads(maxUploads);
        group.setMaxVotes(maxVotes);
        groupMapper.createPolicy(group);

        group.setPrefix(prefix);
        group.setCount(count);
        group.setNoticeSkip(!isAutoNotice);
        createUserList(group);
    }

    public void createUserList(QRGroup group) {
        List<QRUser> userList = IntStream.rangeClosed(1, group.getCount())
            .mapToObj(i -> {
                QRUser user = new QRUser();
                user.setGroupId(group.getGroupId());
                user.setNoticeSkip(group.isNoticeSkip());
                
                user.setQrCode(group.getPrefix() + String.format("%03d", i));
                
                return user;
            })
            .collect(Collectors.toList());

        logger.info("생성된 QRUser 목록: {}", userList);
        userMapper.createUserList(userList);
    }

    @Transactional
    public void updateQRTime(String groupName, Map<String, String> infoMap) {
        Integer maxUploads = Integer.parseInt(infoMap.get("maxUploads"));
        Integer maxVotes = Integer.parseInt(infoMap.get("maxVotes"));

        LocalDateTime uploadStart = LocalDateTime.parse(infoMap.get("uploadStart"));
        LocalDateTime uploadEnd = LocalDateTime.parse(infoMap.get("uploadEnd"));
        LocalDateTime votingStart = LocalDateTime.parse(infoMap.get("votingStart"));
        LocalDateTime votingEnd = LocalDateTime.parse(infoMap.get("votingEnd"));

        QRGroup group = groupMapper.fetchQRGroup(groupName);
        group.setMaxUploads(maxUploads);
        group.setMaxVotes(maxVotes);
        
        group.setUploadStart(uploadStart);
        group.setUploadEnd(uploadEnd);
        group.setVotingStart(votingStart);
        group.setVotingEnd(votingEnd);

        groupMapper.insertQRPolicy(group);

        Boolean isAutoNotice = Boolean.parseBoolean(infoMap.get("autoNotice"));
        List<QRUser> userList = userMapper.fetchQRUserList(groupName);
        userList.stream().map(user -> {
            user.setNoticeSkip(!isAutoNotice);
            user.setType(DMLType.NOTICE.name());

            return user;
        }).collect(Collectors.toList());
        logger.info("공지 스킵 설정된 QRUser 목록: {}", userList);
        userMapper.updateUser(userList);
    }
}
