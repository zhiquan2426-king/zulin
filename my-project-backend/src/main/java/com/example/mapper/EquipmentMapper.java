package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Equipment;
import com.example.entity.vo.request.EquipmentQueryVO;
import com.example.entity.vo.response.EquipmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    
    IPage<EquipmentVO> selectEquipmentPage(Page<?> page, @Param("query") EquipmentQueryVO query);
    
    EquipmentVO selectEquipmentById(Integer id);
    
    @Select("SELECT AVG(rating) FROM db_evaluation WHERE equipment_id = #{equipmentId} AND status = 1")
    Double selectAvgRating(@Param("equipmentId") Integer equipmentId);
    
    @Select("SELECT COUNT(*) FROM db_evaluation WHERE equipment_id = #{equipmentId} AND status = 1")
    Integer selectEvaluationCount(@Param("equipmentId") Integer equipmentId);
}
