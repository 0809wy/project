# CadTable 跨页记忆表格组件

一个基于 Element UI 的 Vue.js 表格组件，支持跨页选中记忆功能。

## 主要特性

✅ **跨页记忆选中** - 翻页时保持选中状态  
✅ **双向数据绑定** - 支持 `selectedRowKeys.sync`  
✅ **灵活配置** - 可自定义行键、分页参数等  
✅ **性能优化** - 高效的选中状态管理  
✅ **完整API** - 丰富的方法和事件  
✅ **数据验证** - 自动清理无效选中项  

## 快速开始

### 基本用法

```vue
<template>
  <cad-table
    :table-column="columns"
    :table-data="tableData"
    :selection="true"
    :selected-row-keys.sync="selectedRowKeys"
    @selection-change="onSelectionChange"
  />
</template>

<script>
export default {
  data() {
    return {
      selectedRowKeys: [], // 选中行的keys
      columns: [
        { prop: 'id', label: 'ID' },
        { prop: 'name', label: '姓名' }
      ],
      tableData: [
        { id: 1, name: '张三' },
        { id: 2, name: '李四' }
      ]
    }
  },
  methods: {
    onSelectionChange(selectedRows) {
      console.log('选中的行:', selectedRows)
    }
  }
}
</script>
```

## Props 参数

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| tableColumn | 表格列配置 | Array | [] |
| tableData | 表格数据 | Array | [] |
| selection | 是否显示选择列 | Boolean | false |
| rowKey | 行数据的唯一标识字段名 | String | 'id' |
| selectedRowKeys | 选中行的keys（支持.sync） | Array | [] |
| needPage | 是否需要分页 | Boolean | true |
| pageSizes | 每页显示条数选择器 | Array | [10, 20, 50, 100] |
| defaultPageSize | 默认每页条数 | Number | 10 |
| defaultPageNum | 默认页码 | Number | 1 |

## Events 事件

| 事件名 | 说明 | 参数 |
|--------|------|------|
| selection-change | 选中项发生变化时触发 | selectedRows |
| update:selectedRowKeys | 选中keys变化时触发（支持.sync） | selectedRowKeys |
| handleSizeChange | 页面大小变化时触发 | pageSize |
| handleCurrentChange | 当前页变化时触发 | currentPage |

## Methods 方法

| 方法名 | 说明 | 参数 |
|--------|------|------|
| clearSelection() | 清空所有选中 | - |
| getSelectedRows() | 获取选中的行数据 | - |
| getSelectedCount() | 获取选中行数量 | - |
| setSelectedRows(keys) | 设置选中行 | rowKeys: Array |
| batchSetSelection(keys, emit) | 批量设置选中（性能优化） | rowKeys: Array, emitEvent: Boolean |
| toggleRowSelection(row, selected) | 切换行选中状态 | row: Object, selected: Boolean |
| selectCurrentPage() | 全选当前页 | - |
| unselectCurrentPage() | 取消选择当前页 | - |
| isRowSelected(row) | 检查行是否已选中 | row: Object |

## 使用示例

### 1. 基本跨页选中

```vue
<template>
  <div>
    <p>已选择: {{ selectedRowKeys.length }} 项</p>
    <cad-table
      :table-column="columns"
      :table-data="tableData"
      :selection="true"
      :selected-row-keys.sync="selectedRowKeys"
      @selection-change="onSelectionChange"
    />
  </div>
</template>
```

### 2. 外部控制选中

```vue
<template>
  <div>
    <el-button @click="selectFirst3">选择前3项</el-button>
    <el-button @click="clearAll">清空选择</el-button>
    
    <cad-table
      ref="table"
      :table-column="columns"
      :table-data="tableData"
      :selection="true"
      :selected-row-keys.sync="selectedRowKeys"
    />
  </div>
</template>

<script>
export default {
  methods: {
    selectFirst3() {
      this.selectedRowKeys = [1, 2, 3]
    },
    clearAll() {
      this.$refs.table.clearSelection()
    }
  }
}
</script>
```

### 3. 自定义行键

```vue
<template>
  <cad-table
    :table-column="columns"
    :table-data="tableData"
    :selection="true"
    row-key="uuid"
    :selected-row-keys.sync="selectedRowKeys"
  />
</template>

<script>
export default {
  data() {
    return {
      tableData: [
        { uuid: 'abc123', name: '张三' },
        { uuid: 'def456', name: '李四' }
      ]
    }
  }
}
</script>
```

## 注意事项

1. **行键唯一性**: 确保 `rowKey` 指定的字段在数据中唯一
2. **数据结构**: 表格数据必须是数组格式
3. **性能优化**: 大量数据时建议使用 `batchSetSelection` 方法
4. **内存管理**: 组件会自动清理无效的选中项

## 核心实现原理

### 跨页记忆机制

1. **状态存储**: 使用 `Set` 存储选中行的ID，提高查找效率
2. **状态同步**: 监听分页变化，自动恢复选中状态
3. **事件防抖**: 防止状态恢复时触发多余事件
4. **数据验证**: 自动清理数据中不存在的选中项

### 关键代码逻辑

```javascript
// 选中状态变化处理
onSelectionChange(rows) {
  // 防止恢复选中时触发事件循环
  if (this.isRestoringSelection) return;
  
  // 清除当前页的所有选中记录
  const currentPageIds = this.pagedData.map(r => r[this.rowKey]);
  currentPageIds.forEach(id => this.selectedRowIds.delete(id));
  
  // 重新添加当前页选中的行
  rows.forEach(row => {
    const id = row[this.rowKey];
    if (id !== undefined && id !== null) {
      this.selectedRowIds.add(id);
    }
  });
  
  // 发射事件
  this.$emit('selection-change', this.getSelectedRows());
  this.$emit('update:selectedRowKeys', Array.from(this.selectedRowIds));
}

// 恢复选中状态
restoreSelection() {
  this.isRestoringSelection = true;
  
  try {
    this.$refs.table.clearSelection();
    this.pagedData.forEach(row => {
      const id = row[this.rowKey];
      if (this.selectedRowIds.has(id)) {
        this.$refs.table.toggleRowSelection(row, true);
      }
    });
  } finally {
    this.isRestoringSelection = false;
  }
}
```

## 更新日志

### v1.0.0
- ✨ 实现基本的跨页选中记忆功能
- ✨ 支持双向数据绑定
- ✨ 添加完整的API方法
- ✨ 性能优化和数据验证
- 📝 完善文档和示例

## License

MIT