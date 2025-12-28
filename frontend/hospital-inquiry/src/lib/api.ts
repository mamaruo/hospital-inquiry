const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ?? 'http://localhost:8081/hi'

export type Role = 'PATIENT' | 'DOCTOR' | 'ADMIN'

export type LoginPayload = {
  mobile: string
  password: string
}

export type SignupPayload = {
  name: string
  mobile: string
  id_card: string
  password: string
}

export type UserResponse = {
  id: number
  name: string
  mobile: string
  id_card: string
  role: Role
  enabled: boolean
}

export type LoginResponse = {
  token: string
  user: UserResponse
}

export type ErrorResponse = {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}

// 科室
export type DepartmentDto = {
  id: number
  name: string
  description: string | null
  doctor_count: number
}

// 医生
export type DoctorDto = {
  id: number
  user_id: number
  name: string
  mobile: string
  department_name: string
  department_id: number
  title: string
  expertise: string
  photo_url: string | null
  available: boolean
}

// 问诊人
export type PatientProfileDto = {
  id: number
  name: string
  gender: string
  birth_date: string | null
  medical_history: string | null
}

export type CreatePatientProfileRequest = {
  name: string
  gender: string
  birth_date: string | null
  medical_history: string | null
}

// 问诊状态
export type InquiryStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'

// 问诊
export type InquiryDto = {
  id: number
  patient_profile: PatientProfileDto
  doctor: DoctorDto
  symptom_description: string
  status: InquiryStatus
  created_at: string
  accepted_at: string | null
  completed_at: string | null
}

export type CreateInquiryRequest = {
  patient_profile_id: number
  doctor_id: number
  symptom_description: string
}

// 消息类型
export type MessageType = 'TEXT' | 'IMAGE'

// 消息
export type MessageDto = {
  id: number
  inquiry_id: number
  sender_id: number
  sender_name: string
  sender_role: string
  type: MessageType
  content: string
  created_at: string
}

async function buildError(response: Response) {
  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    try {
      const data = await response.json() as ErrorResponse
      return data.message || data.error || JSON.stringify(data)
    } catch {
      /* fall through */
    }
  }
  const text = await response.text()
  return text || `请求失败，状态码 ${response.status}`
}

function getToken(): string | null {
  return localStorage.getItem('hospital-inquiry.token')
}

async function request<T>(
  method: string,
  path: string,
  body?: unknown,
  expectJson = true
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  
  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    const message = await buildError(response)
    throw new Error(message)
  }

  if (!expectJson || response.status === 204) {
    return (await response.text()) as T
  }

  return (await response.json()) as T
}

async function post<T>(path: string, body: unknown, expectJson = true): Promise<T> {
  return request<T>('POST', path, body, expectJson)
}

async function get<T>(path: string): Promise<T> {
  return request<T>('GET', path)
}

async function put<T>(path: string, body?: unknown): Promise<T> {
  return request<T>('PUT', path, body)
}

async function del<T>(path: string): Promise<T> {
  return request<T>('DELETE', path, undefined, false)
}

// 认证相关
export function login(payload: LoginPayload) {
  return post<LoginResponse>('/login', payload, true)
}

export function signup(payload: SignupPayload) {
  return post<UserResponse>('/signup', payload, true)
}

// 科室相关
export function getDepartments() {
  return get<DepartmentDto[]>('/api/departments')
}

export function createDepartment(name: string, description: string | null) {
  return post<DepartmentDto>('/api/departments', { name, description })
}

export function updateDepartment(id: number, name: string, description: string | null) {
  return put<DepartmentDto>(`/api/departments/${id}`, { name, description })
}

export function deleteDepartment(id: number) {
  return del<void>(`/api/departments/${id}`)
}

// 医生相关
export function getAvailableDoctors() {
  return get<DoctorDto[]>('/api/doctors/public')
}

export function getDoctorsByDepartment(departmentId: number) {
  return get<DoctorDto[]>(`/api/doctors/public/department/${departmentId}`)
}

export function getDoctorById(id: number) {
  return get<DoctorDto>(`/api/doctors/public/${id}`)
}

export function getAllDoctors() {
  return get<DoctorDto[]>('/api/doctors')
}

export function getDoctorByUserId(userId: number) {
  return get<DoctorDto>(`/api/doctors/user/${userId}`)
}

// 问诊人相关
export function getMyPatientProfiles() {
  return get<PatientProfileDto[]>('/api/patient-profiles')
}

export function getPatientProfileById(id: number) {
  return get<PatientProfileDto>(`/api/patient-profiles/${id}`)
}

export function createPatientProfile(data: CreatePatientProfileRequest) {
  return post<PatientProfileDto>('/api/patient-profiles', data)
}

export function updatePatientProfile(id: number, data: CreatePatientProfileRequest) {
  return put<PatientProfileDto>(`/api/patient-profiles/${id}`, data)
}

export function deletePatientProfile(id: number) {
  return del<void>(`/api/patient-profiles/${id}`)
}

// 问诊相关
export function getPatientInquiries() {
  return get<InquiryDto[]>('/api/inquiries/patient')
}

export function getDoctorInquiries() {
  return get<InquiryDto[]>('/api/inquiries/doctor')
}

export function getPendingInquiries() {
  return get<InquiryDto[]>('/api/inquiries/doctor/pending')
}

export function getInProgressInquiries() {
  return get<InquiryDto[]>('/api/inquiries/doctor/in-progress')
}

export function getInquiryById(id: number) {
  return get<InquiryDto>(`/api/inquiries/${id}`)
}

export function createInquiry(data: CreateInquiryRequest) {
  return post<InquiryDto>('/api/inquiries', data)
}

export function acceptInquiry(id: number) {
  return post<InquiryDto>(`/api/inquiries/${id}/accept`, {})
}

export function completeInquiry(id: number) {
  return post<InquiryDto>(`/api/inquiries/${id}/complete`, {})
}

// 消息相关
export function getMessagesByInquiry(inquiryId: number) {
  return get<MessageDto[]>(`/api/messages/inquiry/${inquiryId}`)
}

// 文件上传
export async function uploadFile(file: File): Promise<{ filename: string; url: string }> {
  const token = getToken()
  const formData = new FormData()
  formData.append('file', file)
  
  const response = await fetch(`${API_BASE_URL}/api/files/upload`, {
    method: 'POST',
    headers: token ? { 'Authorization': `Bearer ${token}` } : {},
    body: formData,
  })
  
  if (!response.ok) {
    const message = await buildError(response)
    throw new Error(message)
  }
  
  return response.json()
}

// 管理员相关
export function getAllPatients() {
  return get<UserResponse[]>('/api/admin/patients')
}

export function getAllDoctorUsers() {
  return get<UserResponse[]>('/api/admin/doctors')
}

export function getAllUsers() {
  return get<UserResponse[]>('/api/admin/users')
}

export function searchUsers(keyword: string) {
  return get<UserResponse[]>(`/api/admin/users/search?keyword=${encodeURIComponent(keyword)}`)
}

export function toggleUserStatus(id: number) {
  return put<UserResponse>(`/api/admin/users/${id}/toggle-status`)
}

export function resetUserPassword(id: number, newPassword: string) {
  return put<void>(`/api/admin/users/${id}/reset-password`, { new_password: newPassword })
}

export function createAdmin(mobile: string, password: string, name: string) {
  return post<UserResponse>('/api/admin/create-admin', { mobile, password, name })
}