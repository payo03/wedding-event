package com.cywedding.service;

import com.cywedding.common.DMLType;
import com.cywedding.dto.QRUser;
import com.cywedding.dto.Vote;
import com.cywedding.mapper.VoteMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    @Autowired
    private ImageService imageService;

    @Autowired
    private QRUserService userService;

    private final VoteMapper voteMapper;

    public void voteImage(String domain, String code, String fileName) {
        imageService.fetchImage(fileName);

        Vote vote = new Vote();
        vote.setQrCode(code);
        vote.setFileName(fileName);
        voteMapper.insertVote(vote);

        QRUser user = new QRUser();
        user.setGroupName(domain);
        user.setQrCode(code);
        user.setType(DMLType.VOTE.name());
        userService.updateUserList(user);
    }

    public void deleteVote(String domain, String code, String fileName) {
        imageService.fetchImage(fileName);

        Vote vote = new Vote();
        vote.setGroupName(domain);
        vote.setQrCode(code);
        voteMapper.deleteVote(vote);
    }
}