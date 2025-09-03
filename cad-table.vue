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
    }
  },
  data() {
    return {
      pageNum: this.defaultPageNum,
      pageSize: this.defaultPageSize,
      selectedRows: [],        // 当前页选中行
      selectedRowIds: new Set() // 跨页存储选中行id
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
    tableData() {
      const maxPage = Math.max(1, Math.ceil(this.tableData.length / this.pageSize));
      if (this.pageNum > maxPage) this.pageNum = maxPage;
      this.$nextTick(() => this.restoreSelection());
    }
  },
  mounted() {
    this.$nextTick(() => {
      const table = this.$refs.table;
      if (table && this.selection) this.restoreSelection();
    });
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
      this.selectedRows = rows || [];

      // 使用配置的行键获取唯一标识
      this.selectedRows.forEach(row => this.selectedRowIds.add(row[this.rowKey]));

      // 当前页未选行移除id
      const currentPageIds = this.pagedData.map(r => r[this.rowKey]);
      currentPageIds.forEach(id => {
        if (!this.selectedRows.find(r => r[this.rowKey] === id)) {
          this.selectedRowIds.delete(id);
        }
      });

      // 同步当前选中行
      this.$emit('selection-change', this.getSelectedRows());
    },
    restoreSelection() {
      if (!this.$refs.table || !this.selection) return;
      this.$refs.table.clearSelection();
      this.pagedData.forEach(row => {
        if (this.selectedRowIds.has(row[this.rowKey])) {
          this.$refs.table.toggleRowSelection(row, true);
        }
      });
      this.selectedRows = this.pagedData.filter(row => this.selectedRowIds.has(row[this.rowKey]));
    },
    getSelectedRows() {
      return this.tableData.filter(row => this.selectedRowIds.has(row[this.rowKey]));
    },
    clearSelection() {
      if (this.$refs.table) this.$refs.table.clearSelection();
      this.selectedRows = [];
      this.selectedRowIds.clear();
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