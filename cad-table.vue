<template>
  <div>
    <!-- 表格 -->
    <el-table
      class="none-border"
      ref="table"
      :data="pagedData"
      border
      v-bind="$attrs"
      v-on="$listeners"
      :header-cell-style="{ background: '#F4F4F4', height: '48px', padding: 0 }"
      :loading="loading"
      @selection-change="onSelectionChange"
    >
      <!-- 固定 selection 列 -->
      <el-table-column
        v-if="selection"
        type="selection"
        width="55"
      ></el-table-column>

      <!-- 普通列 -->
      <el-table-column
        v-for="col in normalColumns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :resizable="true"
        :sortable="col.sortable"
      >
        <!-- 自定义表头 -->
        <template slot="header">
          {{ col.renderHeader ? col.renderHeader() : col.label }}
        </template>

        <!-- 自定义单元格 -->
        <template slot-scope="scope">
          <span v-if="!col.render">{{ scope.row[col.prop] }}</span>
          <template v-else>
            <span v-if="isPrimitive(renderCell(col, scope))">
              {{ renderCell(col, scope) }}
            </span>
            <v-node-renderer v-else :vnode="renderCell(col, scope)" />
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-if="needPage"
      v-bind="$attrs"
      :layout="layout"
      :current-page="pageNum"
      :page-sizes="pageSizes"
      :page-size="pageSize"
      :total="tableData.length"
      background
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script>
const VNodeRenderer = {
  functional: true,
  render(h, { props }) {
    return props.vnode;
  },
  props: {
    vnode: {
      type: Object,
      required: true
    },
  }
};

export default {
  name: "cad-table",
  components: {
    'v-node-renderer': VNodeRenderer
  },
  props: {
    tableColumn: { type: Array, default: () => [] },
    tableData: { type: Array, default: () => [] },
    needPage: { type: Boolean, default: true },
    pageSizes: { type: Array, default: () => [10, 20, 50, 100] },
    layout: { type: String, default: "total, sizes, prev, pager, next, jumper" },
    defaultPageSize: { type: Number, default: 10 },
    defaultPageNum: { type: Number, default: 1 },
    selection: {
      type: Boolean,
      default: false
    },
    rowKey: {
      type: String,
      default: 'id'
    },
    // 外部传入的选中行，支持双向绑定
    selectedRowKeys: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      pageNum: this.defaultPageNum,
      pageSize: this.defaultPageSize,
      selectedRows: [],        // 当前页选中行
      selectedRowIds: new Set(), // 跨页存储选中行id
      isRestoringSelection: false // 防止恢复选中时触发事件循环
    };
  },
  computed: {
    pagedData() {
      if (!this.needPage) return this.tableData;
      const start = (this.pageNum - 1) * this.pageSize;
      return this.tableData.slice(start, start + this.pageSize);
    },
    normalColumns() {
      return this.tableColumn.filter(c => !["selection", "index"].includes(c.type));
    },
  },
  watch: {
    tableData: {
      handler() {
        const maxPage = Math.max(1, Math.ceil(this.tableData.length / this.pageSize));
        if (this.pageNum > maxPage) this.pageNum = maxPage;
        this.$nextTick(() => this.restoreSelection());
      },
      immediate: false
    },
    selectedRowKeys: {
      handler(newKeys) {
        if (Array.isArray(newKeys)) {
          this.selectedRowIds = new Set(newKeys);
          this.$nextTick(() => this.restoreSelection());
        }
      },
      immediate: true
    },
    // 监听分页变化，恢复选中状态
    pageNum() {
      this.$nextTick(() => this.restoreSelection());
    }
  },
  mounted() {
    this.$nextTick(() => {
      const table = this.$refs.table;
      if (table && this.selection) {
        // 验证数据完整性
        this.validateData();
        this.restoreSelection();
      }
    });
  },
  beforeDestroy() {
    // 清理资源
    this.selectedRowIds.clear();
    this.selectedRows = [];
  },
  methods: {
    handleSizeChange(size) {
      this.pageSize = size;
      this.pageNum = 1;
      this.$emit("handleSizeChange", size);
      this.$nextTick(() => this.restoreSelection());
    },
    handleCurrentChange(page) {
      this.pageNum = page;
      this.$nextTick(() => this.restoreSelection());
      this.$emit("handleCurrentChange", page);
    },
    onSelectionChange(rows) {
      // 防止恢复选中时触发事件循环
      if (this.isRestoringSelection) return;
      
      this.selectedRows = rows || [];

      // 获取当前页所有行的ID
      const currentPageIds = this.pagedData.map(r => r[this.rowKey]);
      
      // 先清除当前页在selectedRowIds中的所有记录
      currentPageIds.forEach(id => {
        this.selectedRowIds.delete(id);
      });
      
      // 重新添加当前页选中的行ID
      this.selectedRows.forEach(row => {
        const id = row[this.rowKey];
        if (id !== undefined && id !== null) {
          this.selectedRowIds.add(id);
        }
      });

      // 同步选中行并发射事件
      const allSelectedRows = this.getSelectedRows();
      this.$emit('selection-change', allSelectedRows);
      this.$emit('update:selectedRowKeys', Array.from(this.selectedRowIds));
    },
    restoreSelection() {
      if (!this.$refs.table || !this.selection) return;
      
      this.isRestoringSelection = true;
      
      try {
        // 清除当前选中状态
        this.$refs.table.clearSelection();
        
        // 恢复当前页应该选中的行
        this.pagedData.forEach(row => {
          const id = row[this.rowKey];
          if (id !== undefined && id !== null && this.selectedRowIds.has(id)) {
            this.$refs.table.toggleRowSelection(row, true);
          }
        });
        
        // 更新当前页选中行数组
        this.selectedRows = this.pagedData.filter(row => {
          const id = row[this.rowKey];
          return id !== undefined && id !== null && this.selectedRowIds.has(id);
        });
      } finally {
        this.isRestoringSelection = false;
      }
    },
    getSelectedRows() {
      // 性能优化：如果没有选中行，直接返回空数组
      if (this.selectedRowIds.size === 0) return [];
      
      return this.tableData.filter(row => {
        const id = row[this.rowKey];
        return id !== undefined && id !== null && this.selectedRowIds.has(id);
      });
    },
    clearSelection() {
      if (this.$refs.table) this.$refs.table.clearSelection();
      this.selectedRows = [];
      this.selectedRowIds.clear();
      this.$emit('selection-change', []);
      this.$emit('update:selectedRowKeys', []);
    },
    // 设置选中行（支持外部调用）
    setSelectedRows(rowKeys) {
      if (Array.isArray(rowKeys)) {
        this.selectedRowIds = new Set(rowKeys);
        this.$nextTick(() => this.restoreSelection());
        this.$emit('update:selectedRowKeys', rowKeys);
      }
    },
    // 批量设置选中行（性能优化版本）
    batchSetSelection(rowKeys, emitEvent = true) {
      if (Array.isArray(rowKeys)) {
        this.selectedRowIds = new Set(rowKeys);
        if (emitEvent) {
          this.$emit('update:selectedRowKeys', rowKeys);
          this.$emit('selection-change', this.getSelectedRows());
        }
        this.$nextTick(() => this.restoreSelection());
      }
    },
    // 切换行选中状态
    toggleRowSelection(row, selected) {
      if (this.$refs.table && row) {
        this.$refs.table.toggleRowSelection(row, selected);
      }
    },
    // 获取选中行的数量
    getSelectedCount() {
      return this.selectedRowIds.size;
    },
    // 验证数据完整性
    validateData() {
      if (!Array.isArray(this.tableData)) {
        console.warn('[CadTable] tableData should be an array');
        return false;
      }
      
      // 检查rowKey是否存在于数据中
      if (this.tableData.length > 0) {
        const firstRow = this.tableData[0];
        if (!firstRow.hasOwnProperty(this.rowKey)) {
          console.warn(`[CadTable] rowKey '${this.rowKey}' not found in table data`);
          return false;
        }
      }
      
      // 清理无效的选中行ID（数据中不存在的ID）
      if (this.selectedRowIds.size > 0) {
        const validIds = new Set(this.tableData.map(row => row[this.rowKey]));
        const invalidIds = [];
        
        this.selectedRowIds.forEach(id => {
          if (!validIds.has(id)) {
            invalidIds.push(id);
          }
        });
        
        // 移除无效的ID
        invalidIds.forEach(id => this.selectedRowIds.delete(id));
        
        if (invalidIds.length > 0) {
          console.warn(`[CadTable] Removed ${invalidIds.length} invalid selected row IDs:`, invalidIds);
          this.$emit('update:selectedRowKeys', Array.from(this.selectedRowIds));
        }
      }
      
      return true;
    },
    // 检查行是否已选中
    isRowSelected(row) {
      const id = row[this.rowKey];
      return id !== undefined && id !== null && this.selectedRowIds.has(id);
    },
    // 全选当前页
    selectCurrentPage() {
      if (this.$refs.table) {
        this.pagedData.forEach(row => {
          this.$refs.table.toggleRowSelection(row, true);
        });
      }
    },
    // 取消选择当前页
    unselectCurrentPage() {
      if (this.$refs.table) {
        this.pagedData.forEach(row => {
          this.$refs.table.toggleRowSelection(row, false);
        });
      }
    },
    isPrimitive(value) {
      return ["string", "number"].includes(typeof value);
    },
    renderCell(col, scope) {
      if (!col.render) return null;
      const h = this.$createElement;
      const result = col.render(scope.row, scope.column, scope.$index, h);
      if (typeof result === "string" || typeof result === "number") return result;
      return result;
    },
  }
};
</script>

<style scoped>
.none-border .el-table__header,
.none-border .el-table__body-wrapper {
  border: none;
}
</style>