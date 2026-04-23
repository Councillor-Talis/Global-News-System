package com.gns.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gns.entity.Category;
import com.gns.mapper.CategoryMapper;
import com.gns.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort)
        );
    }
}