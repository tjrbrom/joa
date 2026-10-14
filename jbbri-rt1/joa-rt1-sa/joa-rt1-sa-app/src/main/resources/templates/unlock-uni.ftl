<#import "base.ftl" as base>
<@base.action target="${ontTargetName}" type="ont">
    <intent-specific-data>
        <unlock-uni
            xmlns='http://www.x.com/management-solutions/ont-uni-action'>
            <uni-id>${uniId}</uni-id>
        </unlock-uni>
    </intent-specific-data>
</@base.action>