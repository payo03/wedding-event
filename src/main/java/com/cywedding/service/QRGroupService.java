package com.cywedding.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void createQR(String groupName, String prefix, Integer count, Integer maxUploads, Integer maxVotes) {
        QRGroup group = groupMapper.fetchQRGroup(groupName);

        if (group == null) {
            group = new QRGroup();
            group.setGroupName(groupName);

            groupMapper.createGroup(group);
        } else {
            userMapper.resetUserList(group.getGroupId());
        }
        group.setMaxUploads(maxUploads);
        group.setMaxVotes(maxVotes);
        groupMapper.createPolicy(group);

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

    public void updateQRTime(String groupName, 
            LocalDateTime uploadStart, LocalDateTime uploadEnd,
            LocalDateTime votingStart, LocalDateTime votingEnd
    ) {
        QRGroup group = groupMapper.fetchQRGroup(groupName);
        group.setUploadStart(uploadStart);
        group.setUploadEnd(uploadEnd);
        group.setVotingStart(votingStart);
        group.setVotingEnd(votingEnd);

        groupMapper.insertQRPolicy(group);
    }
}
