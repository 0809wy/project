<template>
    <el-drawer
        title="连接详情"
        :visible.sync="visible"
        :before-close="handleClose"
        :wrapperClosable="false"
        :modal="false"
        :size="800"
    >
        <div class="drawer-content">
            <div class="title">
                <div class="bordered-title" style="padding: 0px;">
                    <div class="border-left" style="margin-left: 0px;"></div>
                    <div class="main-Title">全部会话详情</div>
                </div>
            </div>
            <el-form label-width="100px" class="form">
                <el-row type="flex">
                    <el-col :span="8">
                        <el-form-item label="节点名称：">
                            <el-tooltip placement="top" :content="tableRow.hostName">
                                <span class="text">{{tableRow.hostName || '-'}}</span>
                            </el-tooltip>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="节点IP：">
                            <span class="text">{{tableRow.ipVal || '-'}}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="端口：">
                            <span class="text">{{tableRow.vncPort || '-'}}</span>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row type="flex">
                    <el-col :span="8">
                        <el-form-item label="用户：">
                            <el-tooltip placement="top" :content="tableRow.userName">
                                <span class="text">{{tableRow.userName || '-'}}</span>
                            </el-tooltip>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="会话描述：">
                            <el-tooltip placement="top" :content="tableRow.description">
                                <span class="text">{{tableRow.description || '-'}}
                                </span>
                            </el-tooltip>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="创建时间：">
                            <span>{{tableRow.createTime || '-'}}</span>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="8">
                        <el-form-item label="状态：">
                            <span class="text">{{getState(tableRow.vncSessionStatus)}}</span>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <div class="title">
                <div class="bordered-title" style="padding: 0px;">
                    <div class="border-left" style="margin-left: 0px;"></div>
                    <div class="main-Title">连接列表</div>
                </div>
            </div>
            <div class="search-form">
                <el-button type="primary" @click="disConnect(null, true)" :disabled="!tableSelection.length" :loading="batchLoading">
                    批量断开
                </el-button>
                <searchFormModel
                    ref="search"
                    :multiple="true"
                    filterable
                    :searchList="selectDataList"
                    @on-search="onSearch"
                    :refresh="true"
                />
            </div>
            <Tabel
                class="tableStyle"
                ref="multipleTable"
                :loading="isLoading"
                :total="totalCount"
                :tablePage="tablePage"
                :tableProp="column"
                :tableData="pagedConnectionList"
                :tooltip="true"
                :selectionRowKey="selectionRowKey"
                @handleCurrentChange="handleCurrentChange"
                @handleSizeChange="handleSizeChange"
                :selectionData="tableSelection"
                @handleSortChange="handleSortChange"
                :selectIndeterminate="selectIndeterminate"
                @SelectAll="selectAll"
                @CheckRowSelect="checkRowSelect"
                :allCheck="pageChecked"
                :expand="true"
            >
                <template #usedSlot="{ value }">
                    <div class="usage-cell">
                        <i class="dot"></i><span>{{ value.row.connectionList.length }}</span>
                    </div>
                </template>
                <template #portType="{ value }">
                    <div class="usage-cell">
                        <span>{{value.row.portType == 'webvnc-port' ? 'WebVnc' : 'client'}}</span>
                    </div>
                </template>
                <template #operation="{ value }">
                <el-button type="text" :disabled="value.row.portType == 'vncserver-port'" @click="disConnect(value.row)"> 断开 </el-button>
                </template>
            </Tabel>
        </div>
     
    </el-drawer>
</template>

<script>
import searchFormModel from '../../../../components/common/searchFormModel.vue';
import Tabel from '@/components/tablePublic/index.vue';
import service from "@/api/sessionManagement/conver.js"
import tableSelectionMixin from '@/mixins/tableSelectionMixin.js'
import conversationList from './conversationList.vue';
    export default {
        components: {
            searchFormModel,
            Tabel
        },
        mixins: [tableSelectionMixin],
        data() {
            return {
                // 通用选择混入配置（基于当前页数据计算全选/半选）
                selectionDataKey: 'pagedConnectionList',
                selectionRowKey: 'pid',
                selectDataList: [
                    { label: '客户端IP', value: 'clientIp'},
                    { label: '来源', value: 'portType', type:'select', data: [{label:'WebVnc', value: 'webvnc-port'}, {label: 'client', value: 'vncserver-port'}]},
                ],
                tablePage:{
                    current:1,
                    size: 10
                },
                column: [
                    {
                        type: 'selectionCheck',
                        width: 55,
                        align: 'center',
                    },
                    {
                        label: '客户端IP',
                        prop: 'clientIp',
                        align: 'center',
                    },
                    {
                        label: '来源',
                        prop: 'portType',
                        slot: 'portType',
                        align: 'center',
                    },
                    {
                        label: ' 连接时间',
                        prop: 'connectTime',
                        align: 'center',
                        sort: {prop: 'connectTime', order: 'descending'}
                    },
                    {
                        label: '操作',
                        prop: 'operation', 
                        slot: 'operation'
                    },
                ],
                tableData: [],
                // 本地展示和分页数据（不影响父组件）
                connectionList: [],
                sourceConnectionList: [],
                displayConnectionList: [],
                pagedConnectionList: [],
                isLoading: false,
                // 搜索筛选参数
                filterParams: {},
                batchLoading: false,
            }
        },
        computed: {
            totalCount() {
                return (this.displayConnectionList || []).length;
            }
        },
        created() {
            this.getSessionDetail();
        },
        mounted() {
            this.$nextTick(() => {
                this.resetDisplayList();
                this.syncSelectionFlags();
            });
        },
        props: ['visible','tableRow'],
        watch: {
            connectionList: {
                handler() {
                    this.$nextTick(() => {
                        this.resetDisplayList();
                        this.syncSelectionFlags();
                    });
                },
                deep: true
            }
        },
        methods: {
            handleSortChange(column, prop, order) {
                // 如果没有排序属性或者没有数据，直接返回
                if (!prop || !this.displayConnectionList.length) {
                    return;
                }

                // 根据排序规则对显示列表进行排序
                this.displayConnectionList.sort((a, b) => {
                    let aVal = a[prop];
                    let bVal = b[prop];

                    // 处理空值
                    if (aVal === null || aVal === undefined) aVal = '';
                    if (bVal === null || bVal === undefined) bVal = '';

                    // 如果是时间字段，转换为时间戳进行比较
                    if (prop === 'connectTime') {
                        aVal = new Date(aVal).getTime() || 0;
                        bVal = new Date(bVal).getTime() || 0;
                    }

                    // 数值类型比较
                    if (typeof aVal === 'number' && typeof bVal === 'number') {
                        if (order === 'ascending') {
                            return aVal - bVal;
                        } else {
                            return bVal - aVal;
                        }
                    }

                    // 字符串类型比较
                    const aStr = String(aVal).toLowerCase();
                    const bStr = String(bVal).toLowerCase();
                    
                    if (order === 'ascending') {
                        return aStr.localeCompare(bStr);
                    } else {
                        return bStr.localeCompare(aStr);
                    }
                });

                // 排序后回到第一页
                this.tablePage.current = 1;
                
                // 重新构建分页数据
                this.rebuildPagedData();
            },
            handleClose() {
                this.$emit('closeSesstionDrwer')
            },
            getSessionDetail(callback) {
                service.getSessionDetail({id: this.tableRow.id}).then(res=> {
                    if (res.status !== 200) {
                        return
                    }
                    if (res.data.code !== 200) {
                        return
                    }
                    this.connectionList = res.data.data.connectionList;
                    callback && callback();
                })
            },
            onSearch(params) {
                this.isLoading = true;
                // 接收 searchFormModel 输出的对象，如 { localIp: '1.1.1', portType: 'webVnc' }
                this.filterParams = params || {};
                this.getSessionDetail(()=> {
                    this.isLoading = false;
                    this.applyFilter();
                    // 搜索后从第一页开始
                    this.tablePage.current = 1;
                    this.rebuildPagedData();
                })
            },
            getState(state) {
                return this.$parent.getState(state)
            },
            // 重置本地展示列表与分页
            resetDisplayList() {
                // 深拷贝父数据，避免影响父组件
                const source = Array.isArray(this.connectionList) ? this.connectionList : [];
                this.sourceConnectionList = source.map(item => ({ ...item }));
                this.applyFilter();
                // 保持当前页有效
                const maxPage = Math.max(1, Math.ceil(this.displayConnectionList.length / this.tablePage.size));
                if (this.tablePage.current > maxPage) this.tablePage.current = maxPage;
                this.rebuildPagedData();
            },
            // 应用筛选条件
            applyFilter() {
                const params = this.filterParams || {};
                const keys = Object.keys(params).filter(k => params[k] !== undefined && params[k] !== null && params[k] !== '');
                if (!keys.length) {
                    this.displayConnectionList = this.connectionList.slice();
                    return;
                }
                this.displayConnectionList = this.connectionList.filter(row => {
                    return keys.every(key => {
                        const filterVal = params[key];
                        const rowVal = row[key];
                        if (Array.isArray(filterVal)) {
                            // 多选
                            return filterVal.includes(rowVal);
                        }
                        if (typeof filterVal === 'string') {
                            // 模糊匹配
                            return (rowVal !== undefined && rowVal !== null) && String(rowVal).includes(filterVal);
                        }
                        return rowVal === filterVal;
                    });
                });
            },
            // 根据当前分页重建当前页数据
            rebuildPagedData() {
                const start = (this.tablePage.current - 1) * this.tablePage.size;
                const end = start + this.tablePage.size;
                this.pagedConnectionList = this.displayConnectionList.slice(start, end).map(item=>{
                     if (item.portType === 'vncserver-port') {
                        item.disabled = true
                     } else {
                        item.disabled = false
                     }
                     return item
                });
                // 重新同步全选/半选状态
                this.syncSelectionFlags();
            },
            // 分页回调
            handleCurrentChange(currentPage) {
                this.tablePage.current = currentPage;
                this.rebuildPagedData();
            },
            handleSizeChange(size) {
                this.tablePage.size = size;
                this.tablePage.current = 1;
                this.rebuildPagedData();
            },
            // 断开连接：type 为 true 表示批量
            disConnect(row, type) {
                const selectedRows = this.getSelectedRows();
                let connections = [];
                if (type) {
                    if (Array.isArray(selectedRows) && selectedRows.length) {
                        connections = selectedRows.map(item => ({
                            clientIp: item.clientIp || item.localIp,
                            clientPort: item.clientPort,
                            port: item.port,
                        }));
                    }
                } else if (row) {
                    connections = [{
                        clientIp: row.clientIp || row.localIp,
                        clientPort: row.clientPort,
                        port: row.port,
                    }];
                }
                if (!connections.length) return;
                const params = { connections, nodeIp: this.tableRow.ipVal };
                this.batchLoading = true;
                this.$confirm('是否要断开所选会话?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(res=> {
                    service.killConnections(params).then(res => {
                        if (res.status !== 200) {
                            this.$notify.error({title: "提示",message: "请求失败"});
                            return;
                        }
                        if (res.data.code !== 200) {
                            this.$notify.error({title: "提示",message: res.data.message});
                            return;
                        }
                        this.getSessionDetail(); // 重新获取数据
                    }).finally(() => {
                        this.batchLoading = false;
                    });
                }).finally(item=> {
                    this.batchLoading = false;
                })
               
            },
        }
    }
</script>

<style lang="less">
    .drawer-content {
        width: 93%;
        margin: 0 auto;
        .title {
            margin-bottom: 20px;
            .border-left {
                flex-grow: unset;
            }
        }
        .search-form {
            display: flex;
            justify-content: space-between;
        }
    }
    .form {
        .text {
            display: inline-block;
            width: 120px;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
    }
</style>