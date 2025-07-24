package com.cywedding.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cywedding.dto.Vote;

@Mapper
public interface VoteMapper {

    List<Vote> selectVoteList(String fileName);

    void insertVote(Vote vote);

    void deleteVote(Vote vote);
}
