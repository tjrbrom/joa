{{/*
Expand the name of the chart.
*/}}
{{- define "joa-rt1agent.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "joa-rt1agent.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "joa-rt1agent.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "joa-rt1agent.labels" -}}
helm.sh/chart: {{ include "joa-rt1agent.chart" . }}
{{ include "joa-rt1agent.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "joa-rt1agent.selectorLabels" -}}
app: {{ .Chart.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "joa-rt1agent.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "joa-rt1agent.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}


{{/*
securityMode env
*/}}
{{- define "securityMode.config" -}}
{{- if .Values.env.securityMode -}}
- name: JOA_RT1_SECURITY_MODE
  value: {{ .Values.env.securityMode }}
{{- else if .Values.global.securityMode -}} 
- name: JOA_RT1_SECURITY_MODE
  value: {{ .Values.global.securityMode }}
{{- end }}
{{- end }}

{{/*
keycloakUrl env
*/}}
{{- define "keycloakUrl.config" -}}
{{- if .Values.env.keycloakUrl -}}
- name: JOA_RT1_SECURITY_KEYCLOAK_AUTHSERVERURL
  value: {{ .Values.env.keycloakUrl }}
{{- else if .Values.global.keycloakUrl -}}
- name: JOA_RT1_SECURITY_KEYCLOAK_AUTHSERVERURL
  value: {{ .Values.global.keycloakUrl }}
{{- end }}
{{- end }}