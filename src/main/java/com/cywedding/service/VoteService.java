package com.cywedding.service;

import com.cywedding.common.DMLType;
import com.cywedding.dto.QRUser;
import com.cywedding.dto.Vote;
import com.cywedding.mapper.VoteMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

    @Autowired
    private QRUserService userService;

    @Autowired
    private ImageService imageService;

    private final VoteMapper voteMapper;

    @Transactional
    public void voteImage(String domain, String code, String fileName) {
        imageService.fetchImage(fileName);
        QRUser user = userService.fetchQRUser(domain, code);

        Vote vote = new Vote();
        vote.setGroupId(user.getCustomGroupId());
        vote.setQrCode(code);
        vote.setFileName(fileName);
        voteMapper.insertVote(vote);

        QRUser param = new QRUser();
        param.setGroupId(user.getCustomGroupId());
        param.setQrCode(code);
        param.setType(DMLType.VOTE.name());
        userService.updateUser(param);
    }

    @Transactional
    public void deleteVote(String domain, String code, String fileName) {
        imageService.fetchImage(fileName);
        QRUser user = userService.fetchQRUser(domain, code);

        Vote vote = new Vote();
        vote.setGroupId(user.getCustomGroupId());
        vote.setGroupName(domain);
        vote.setQrCode(code);
        voteMapper.deleteVote(vote);

        QRUser param = new QRUser();
        param.setGroupId(user.getCustomGroupId());
        param.setQrCode(code);
        param.setType(DMLType.VOTE_CANCEL.name());
        userService.updateUser(param);
    }
}