{{/*
Expand the name of the chart.
*/}}
{{- define "joa-mds.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "joa-mds.fullname" -}}
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
{{- define "joa-mds.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "joa-mds.labels" -}}
helm.sh/chart: {{ include "joa-mds.chart" . }}
{{ include "joa-mds.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "joa-mds.selectorLabels" -}}
app: {{ .Chart.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "joa-mds.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "joa-mds.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
joa-mds service name
*/}}
{{- define "joa-mds.service.name" -}}
{{- if ne .Values.service.nameOverride ""}}
{{- printf "%s" .Values.service.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s" (include "joa-mds.fullname" .) -}}
{{- end -}}
{{- end }}

{{/*
joa-mds keys name
*/}}
{{- define "keys.volumes.all-keys-storage" -}}
- name: component-all-keys-storage
  secret:
    secretName:
      {{ .Values.global.externalKeys.secretName | quote}}
{{- end -}}