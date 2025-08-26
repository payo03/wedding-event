package com.cywedding.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cywedding.dto.Vote;

@Mapper
public interface VoteMapper {

    List<Vote> selectVoteList(@Param("fileName") String fileName);

    void insertVote(Vote vote);

    void deleteVote(Vote vote);
}
