<template>
  <div class="app-container">
    <h4 class="form-header h4">用户信息</h4>
    <el-form ref="form" :model="form" label-width="80px">
      <el-row>
        <el-col :span="8" :offset="2">
          <el-form-item label="用户昵称" prop="nickName">
            <el-input v-model="form.nickName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="8" :offset="2">
          <el-form-item label="登录账号" prop="userName">
            <el-input  v-model="form.userName" disabled />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <h4 class="form-header h4">数据库信息</h4>
    <el-table v-loading="loading" :row-key="getRowKey" @row-click="clickRow" ref="table" @selection-change="handleSelectionChange" :data="roles.slice((pageNum-1)*pageSize,pageNum*pageSize)">
      <el-table-column label="序号" type="index" align="center">
        <template slot-scope="scope">
          <span>{{(pageNum - 1) * pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column type="selection" :reserve-selection="true" width="55"></el-table-column>
      <!-- <el-table-column label="数据库ID" align="center" prop="id" width="85" /> -->
      <el-table-column label="数据库名称" align="center" prop="databaseName" />
      <el-table-column label="IP" align="center" prop="ip" />
      <el-table-column label="端口" align="center" prop="port" />
      <el-table-column label="schema" align="center" prop="schemaName" />
      <el-table-column label="数据库账号" align="center" prop="username" ></el-table-column>
      <el-table-column label="密码" align="center" prop="password" />
      <!-- <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column> -->
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="pageNum" :limit.sync="pageSize" />

    <el-form label-width="100px">
      <el-form-item style="text-align: center;margin-left:-120px;margin-top:30px;">
        <el-button type="primary" @click="submitForm()">提交</el-button>
        <el-button @click="close()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getSettingDb, updateSettingDb } from "@/api/user/pptuser";

export default {
  name: "settingDb",
  data() {
    return {
       // 遮罩层
      loading: true,
      // 分页信息
      total: 0,
      pageNum: 1,
      pageSize: 10,
      // 选中数据库ID
      dBs:[],
      // 角色信息
      roles: [],
      // 用户信息
      form: {}
    };
  },
  created() {
    const userId = this.$route.params && this.$route.params.userId;
    if (userId) {
      this.loading = true;
      getSettingDb(userId).then((response) => {
        this.form = response.user;
        this.roles = response.databases;
        this.total = this.roles.length;
        this.$nextTick(() => {
          this.roles.forEach((row) => {
            if (row.flag) {
              this.$refs.table.toggleRowSelection(row);
            }
          });
        });
        this.loading = false;
      });
    }
  },
  methods: {
    /** 单击选中行数据 */
    clickRow(row) {
      this.$refs.table.toggleRowSelection(row);
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.dBs = selection.map((item) => item.id);
    },
    // 保存选中的数据编号
    getRowKey(row) {
      return row.id;
    },
    /** 提交按钮 */
    submitForm() {
      const userId = this.form.userId;
      const dBs = this.dBs.join(",");
      // alert(dBs+userId);
      updateSettingDb({ userId: userId, dBs: dBs }).then((response) => {
        this.$modal.msgSuccess("授权成功");
        this.close();
      });
    },
    /** 关闭按钮 */
    close() {
      const obj = { path: "/ppt_manager/pptuser" };
      this.$tab.closeOpenPage(obj);
    },
  },
};
</script>