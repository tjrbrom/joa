<#import "base.ftl" as base>
<@base.action target="${intentTarget}" type="${intentType}">
    <delete-dependents>
        <delete-option>Both</delete-option>
        <include-this-intent>true</include-this-intent>
        <all-dependents></all-dependents>
    </delete-dependents>
</@base.action>