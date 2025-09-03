<template>
  <div>
    <h2>表格跨页记忆选择示例</h2>
    
    <!-- 操作按钮 -->
    <div style="margin-bottom: 16px;">
      <el-button @click="clearAllSelection">清空所有选择</el-button>
      <el-button @click="selectFirstThree">选择前3条</el-button>
      <el-button @click="showSelected">查看已选择</el-button>
      <span style="margin-left: 16px;">
        已选择: {{ selectedCount }} 条记录
      </span>
    </div>

    <!-- 表格组件 -->
    <cad-table
      ref="cadTable"
      :table-column="columns"
      :table-data="tableData"
      :selection="true"
      :need-page="true"
      :default-page-size="5"
      row-key="id"
      :selected-row-keys.sync="selectedRowKeys"
      @selection-change="onSelectionChange"
    />

    <!-- 选中行详情 -->
    <div v-if="selectedRows.length > 0" style="margin-top: 16px;">
      <h3>已选择的记录：</h3>
      <ul>
        <li v-for="row in selectedRows" :key="row.id">
          ID: {{ row.id }}, 姓名: {{ row.name }}, 年龄: {{ row.age }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import CadTable from './cad-table.vue'

export default {
  name: 'TableExample',
  components: {
    CadTable
  },
  data() {
    return {
      selectedRowKeys: [], // 选中行的keys，支持双向绑定
      selectedRows: [],    // 选中的完整行数据
      columns: [
        {
          prop: 'id',
          label: 'ID',
          width: 80
        },
        {
          prop: 'name',
          label: '姓名',
          width: 120
        },
        {
          prop: 'age',
          label: '年龄',
          width: 80
        },
        {
          prop: 'city',
          label: '城市',
          width: 120
        },
        {
          prop: 'email',
          label: '邮箱',
          width: 200
        }
      ],
      // 模拟数据
      tableData: [
        { id: 1, name: '张三', age: 25, city: '北京', email: 'zhangsan@example.com' },
        { id: 2, name: '李四', age: 30, city: '上海', email: 'lisi@example.com' },
        { id: 3, name: '王五', age: 28, city: '广州', email: 'wangwu@example.com' },
        { id: 4, name: '赵六', age: 32, city: '深圳', email: 'zhaoliu@example.com' },
        { id: 5, name: '钱七', age: 26, city: '杭州', email: 'qianqi@example.com' },
        { id: 6, name: '孙八', age: 29, city: '南京', email: 'sunba@example.com' },
        { id: 7, name: '周九', age: 31, city: '武汉', email: 'zhoujiu@example.com' },
        { id: 8, name: '吴十', age: 27, city: '成都', email: 'wushi@example.com' },
        { id: 9, name: '郑一', age: 33, city: '重庆', email: 'zhengyi@example.com' },
        { id: 10, name: '王二', age: 24, city: '西安', email: 'wanger@example.com' },
        { id: 11, name: '李三', age: 35, city: '青岛', email: 'lisan@example.com' },
        { id: 12, name: '张四', age: 28, city: '大连', email: 'zhangsi@example.com' }
      ]
    }
  },
  computed: {
    selectedCount() {
      return this.selectedRowKeys.length
    }
  },
  methods: {
    // 选择变化事件
    onSelectionChange(rows) {
      this.selectedRows = rows
      console.log('选中行数据:', rows)
      console.log('选中行keys:', this.selectedRowKeys)
    },
    
    // 清空所有选择
    clearAllSelection() {
      this.$refs.cadTable.clearSelection()
    },
    
    // 选择前3条记录
    selectFirstThree() {
      this.selectedRowKeys = [1, 2, 3]
    },
    
    // 显示已选择的记录
    showSelected() {
      this.$message({
        message: `已选择 ${this.selectedCount} 条记录`,
        type: 'info'
      })
    }
  }
}
</script>

<style scoped>
h2 {
  color: #409eff;
  margin-bottom: 20px;
}

h3 {
  color: #606266;
  margin: 16px 0 8px 0;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  padding: 4px 0;
  border-bottom: 1px solid #ebeef5;
}
</style>