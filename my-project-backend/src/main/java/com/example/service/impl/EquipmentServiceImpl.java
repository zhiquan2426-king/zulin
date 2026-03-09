package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Equipment;
import com.example.entity.Order;
import com.example.mapper.EquipmentMapper;
import com.example.mapper.OrderMapper;
import com.example.service.EquipmentService;
import com.example.entity.vo.request.EquipmentAddVO;
import com.example.entity.vo.request.EquipmentQueryVO;
import com.example.entity.vo.response.EquipmentVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Resource
    private EquipmentMapper equipmentMapper;
    
    @Resource
    private OrderMapper orderMapper;

    @Override
    public IPage<EquipmentVO> getEquipmentPage(EquipmentQueryVO query) {
        Page<EquipmentVO> page = new Page<>(query.getPage(), query.getSize());
        return equipmentMapper.selectEquipmentPage(page, query);
    }

    @Override
    public EquipmentVO getEquipmentById(Integer id) {
        return equipmentMapper.selectEquipmentById(id);
    }

    @Override
    @Transactional
    public boolean addEquipment(EquipmentAddVO vo, Integer adminId) {
        Equipment equipment = new Equipment();
        equipment.setName(vo.getName());
        equipment.setCategory(vo.getCategory());
        equipment.setBrand(vo.getBrand());
        equipment.setModel(vo.getModel());
        equipment.setDescription(vo.getDescription());
        equipment.setDailyPrice(vo.getDailyPrice());
        equipment.setDeposit(vo.getDeposit());
        equipment.setStock(vo.getStock());
        equipment.setAvailable(vo.getStock());
        equipment.setImage(vo.getImage());
        equipment.setStatus(vo.getStatus());
        equipment.setAdminId(adminId);
        return save(equipment);
    }

    @Override
    @Transactional
    public boolean updateEquipment(Integer id, EquipmentAddVO vo, Integer adminId) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName(vo.getName());
        equipment.setCategory(vo.getCategory());
        equipment.setBrand(vo.getBrand());
        equipment.setModel(vo.getModel());
        equipment.setDescription(vo.getDescription());
        equipment.setDailyPrice(vo.getDailyPrice());
        equipment.setDeposit(vo.getDeposit());
        
        Equipment existing = getById(id);
        int availableDiff = vo.getStock() - existing.getStock();
        equipment.setAvailable(existing.getAvailable() + availableDiff);
        
        equipment.setStock(vo.getStock());
        equipment.setImage(vo.getImage());
        equipment.setStatus(vo.getStatus());
        equipment.setAdminId(adminId);
        return updateById(equipment);
    }

    @Override
    @Transactional
    public boolean deleteEquipment(Integer id) {
        // 检查是否有进行中的订单
        long activeOrderCount = orderMapper.selectCount(new QueryWrapper<Order>()
                .eq("equipment_id", id)
                .in("status", "paid", "renting", "overdue"));
        if (activeOrderCount > 0) {
            throw new RuntimeException("该设备有进行中的订单，无法删除");
        }
        return removeById(id);
    }

    @Override
    public boolean updateEquipmentStatus(Integer id, Integer status, Integer adminId) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setStatus(status);
        equipment.setAdminId(adminId);
        return updateById(equipment);
    }

    @Override
    @Transactional
    public boolean updateAvailable(Integer equipmentId, Integer quantity) {
        Equipment equipment = getById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        int newAvailable = equipment.getAvailable() + quantity;
        if (newAvailable < 0) {
            throw new RuntimeException("可用数量不足");
        }
        if (newAvailable > equipment.getStock()) {
            newAvailable = equipment.getStock();
        }
        
        equipment.setAvailable(newAvailable);
        return updateById(equipment);
    }
}
