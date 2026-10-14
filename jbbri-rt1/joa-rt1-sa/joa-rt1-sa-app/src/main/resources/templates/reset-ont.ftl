<#import "base.ftl" as base>
<@base.action target="${ontTargetName}" type="ont">
    <intent-specific-data>
        <reset-ont
            xmlns='http://www.x.com/management-solutions/ont-action'>
            <reset>active-image</reset>
        </reset-ont>
    </intent-specific-data>
</@base.action>