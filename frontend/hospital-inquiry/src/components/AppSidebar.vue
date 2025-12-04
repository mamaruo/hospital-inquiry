<script setup lang="ts">
import type { SidebarProps } from '@/components/ui/sidebar'

import {
  BookOpen,
  Bot,
  Frame,
  GalleryVerticalEnd,
  Map,
  PieChart,
  Settings2,
  SquareTerminal,
} from "lucide-vue-next"
import NavMain from '@/components/NavMain.vue'
import NavProjects from '@/components/NavProjects.vue'
import NavUser from '@/components/NavUser.vue'
import {
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
} from '@/components/ui/sidebar'

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
} from '@/components/ui/sidebar'

const props = withDefaults(defineProps<SidebarProps>(), {
  collapsible: "icon",
})

// This is sample data.
const data = {
  user: {
    name: "张三",
    email: "13700137003",
    avatar: "/avatars/zhang.jpg",
  },
  teams: [
    {
      name: "医院",
      logo: GalleryVerticalEnd,
      plan: "综合医院",
    }
  ],
  navMain: [
    {
      title: "预约挂号",
      url: "#",
      icon: SquareTerminal,
      isActive: true,
      items: [
        {
          title: "在线预约",
          url: "#",
        },
        {
          title: "挂号记录",
          url: "#",
        },
        {
          title: "取消预约",
          url: "#",
        },
      ],
    },
    {
      title: "医生咨询",
      url: "#",
      icon: Bot,
      items: [
        {
          title: "在线问诊",
          url: "#",
        },
        {
          title: "咨询历史",
          url: "#",
        },
        {
          title: "专家推荐",
          url: "#",
        },
      ],
    },
    {
      title: "病历管理",
      url: "#",
      icon: BookOpen,
      items: [
        {
          title: "查看病历",
          url: "#",
        },
        {
          title: "上传报告",
          url: "#",
        },
        {
          title: "健康档案",
          url: "#",
        },
        {
          title: "数据导出",
          url: "#",
        },
      ],
    },
    {
      title: "设置",
      url: "#",
      icon: Settings2,
      items: [
        {
          title: "个人资料",
          url: "#",
        },
        {
          title: "隐私设置",
          url: "#",
        },
        {
          title: "通知偏好",
          url: "#",
        },
        {
          title: "账户管理",
          url: "#",
        },
      ],
    },
  ],
  projects: [
    {
      name: "门诊服务",
      url: "#",
      icon: Frame,
    },
    {
      name: "住院管理",
      url: "#",
      icon: PieChart,
    },
    {
      name: "健康监测",
      url: "#",
      icon: Map,
    },
  ],
}
</script>

<template>
  <Sidebar v-bind="props">
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton size="lg">
            <img src="@/assets/icon.svg" alt="问诊系统图标" class="h-6 w-6" />
            <span class="text-lg font-semibold">问诊系统</span>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>
    <SidebarContent>
      <NavMain :items="data.navMain" />
      <NavProjects :projects="data.projects" />
    </SidebarContent>
    <SidebarFooter>
      <NavUser :user="data.user" />
    </SidebarFooter>
    <SidebarRail />
  </Sidebar>
</template>