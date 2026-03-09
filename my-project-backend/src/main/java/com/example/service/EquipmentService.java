package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.vo.request.EquipmentAddVO;
import com.example.entity.vo.request.EquipmentQueryVO;
import com.example.entity.vo.response.EquipmentVO;

public interface EquipmentService {
    
    IPage<EquipmentVO> getEquipmentPage(EquipmentQueryVO query);
    
    EquipmentVO getEquipmentById(Integer id);
    
    boolean addEquipment(EquipmentAddVO vo, Integer adminId);
    
    boolean updateEquipment(Integer id, EquipmentAddVO vo, Integer adminId);
    
    boolean deleteEquipment(Integer id);
    
    boolean updateEquipmentStatus(Integer id, Integer status, Integer adminId);
    
    boolean updateAvailable(Integer equipmentId, Integer quantity);
}
