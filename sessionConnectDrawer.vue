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
            handleSortChange(column,prop,order) {
				// 兼容不同事件签名：可能是 (column, prop, order) 或 ({ column, prop, order })
				const sortProp = prop || (column && (column.prop || column.property)) || (column && column.prop);
				let sortOrder = order || (column && column.order);
				if (typeof sortOrder === 'string') {
					sortOrder = sortOrder.toLowerCase();
					if (sortOrder === 'asc') sortOrder = 'ascending';
					if (sortOrder === 'desc') sortOrder = 'descending';
				}

				// 未指定排序或清空排序：恢复筛选后的原始顺序
				if (!sortProp || !sortOrder) {
					this.applyFilter();
					// 保持当前页有效
					const maxPage = Math.max(1, Math.ceil(this.displayConnectionList.length / this.tablePage.size));
					if (this.tablePage.current > maxPage) this.tablePage.current = maxPage;
					this.rebuildPagedData();
					return;
				}

				const isNullish = (v) => v === undefined || v === null || v === '';
				const tryParseNumber = (v) => {
					if (typeof v === 'number') return v;
					const n = Number(v);
					return Number.isNaN(n) ? null : n;
				};
				const tryParseDate = (v) => {
					if (v instanceof Date) return v.getTime();
					if (typeof v === 'string' || typeof v === 'number') {
						const t = Date.parse(v);
						return Number.isNaN(t) ? null : t;
					}
					return null;
				};

				const compare = (a, b) => {
					const av = a[sortProp];
					const bv = b[sortProp];
					const aNull = isNullish(av);
					const bNull = isNullish(bv);
					if (aNull && bNull) return 0;
					if (aNull) return 1; // 空值永远排最后
					if (bNull) return -1;

					// 数字比较（包含可转数字的字符串）
					const an = tryParseNumber(av);
					const bn = tryParseNumber(bv);
					if (an !== null && bn !== null) {
						if (an === bn) return 0;
						return an < bn ? -1 : 1;
					}

					// 日期时间比较
					const ad = tryParseDate(av);
					const bd = tryParseDate(bv);
					if (ad !== null && bd !== null) {
						if (ad === bd) return 0;
						return ad < bd ? -1 : 1;
					}

					// 字符串比较（不区分大小写）
					const as = String(av).toLowerCase();
					const bs = String(bv).toLowerCase();
					if (as === bs) return 0;
					return as < bs ? -1 : 1;
				};

				const sorted = (this.displayConnectionList || []).slice().sort((a, b) => {
					const base = compare(a, b);
					return sortOrder === 'ascending' ? base : -base;
				});

				this.displayConnectionList = sorted;
				// 改变排序后回到第一页
				this.tablePage.current = 1;
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