<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 
 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"
 
 Contributor(s):
 
 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
-->
<!-- applications/resourcesTable.inc -->

<sun:table id="resourcesTable" title="#{tableTitle}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
    
    <!facet actionsTop>
         <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />

            <sun:button id="newButton" text="$resource{i18n.button.New}" >
                <!command redirect(page="#{createLink}"); />
            </sun:button>
              
            <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
                    onClick="if ( getConfirm(this, '$resource{i18n.msg.JS.confirmDeleteCallFlowData}') )
                    { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                    else {return false;} "
            >
            <!command 
                getUIComponent(clientId="$pageSession{rowGroupId}", component=>$attribute{tableRowGroup});      
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                deleteResource(selectedRows="${selectedRows}" resourceType="#{resourceType}" isJmsConnectionFactory="#{isJmsConnectionFactory}" );
                setAttribute(key="selectedRows" value="${selectedRows}");
                navigate(page="#{listPageLink}");
            />
           
            </sun:button>
            
            <sun:button id="button2" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}">
                <!command 
                    getUIComponent(clientId="$pageSession{rowGroupId}", component=>$attribute{tableRowGroup});      
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    changeResourceStatus(selectedRows="${selectedRows}" resourceType="#{resourceType}" enabled="#{true}" )
                    setAttribute(key="selectedRows" value="${selectedRows}");
                    navigate(page="#{listPageLink}");
                />
                
            </sun:button>
            <sun:button id="button3" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}">
                <!command 
                    getUIComponent(clientId="$pageSession{rowGroupId}", component=>$attribute{tableRowGroup});      
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    changeResourceStatus(selectedRows="${selectedRows}" resourceType="#{resourceType}" enabled="#{false}" )
                    setAttribute(key="selectedRows" value="${selectedRows}");
                    navigate(page="#{listPageLink}");
                />
            </sun:button>
            <sun:image id="actionTopSeparator1" rendered="$session{supportCluster}"
               align="top"
               height="$int{18}"
               url="/theme/com/sun/webui/jsf/suntheme/images/table/actions_separator.gif"
               width="$int{18}"
               />

            <sun:dropDown id="view"  rendered="$session{supportCluster}" selected="$pageSession{viewValue}" label="$resource{i18n.common.viewDropDown}" submitForm="#{true}" immediate="#{true}" 
                    labels={"$resource{i18n.common.Summary}", "$resource{i18n.common.Detailed}" }
                    values={"summary", "detail"} >
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAppViewKey(key="$pageSession{viewKey}" selectedValue="#{click.selected}");
                    navigate(page="#{listPageLink}");
                />
            </sun:dropDown>
         </sun:panelGroup>
     </facet>

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{rowGroupId});
        />
  
   <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('initAllRows(); changeThreeTableButtons()', 0)" 
            />
    </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18n.resourcesTable.colJndiName}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" 
            url="#{editLink}?name=#{td.value.name}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="#{supportCluster}" headerText="$resource{i18n.deployTable.colStatus}" sort="enabled" rowHeader="$boolean{true}" id="col2">
        <sun:hyperlink rendered="#{supportCluster}" id="targetLink" text="#{td.value.enabled}" url="#{targetLink}?name=#{td.value.name}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="#{notSupportCluster}" headerText="$resource{i18n.deployTable.colEnabled}" sort="enabled" rowHeader="$boolean{true}" id="col22">
        <staticText id="typeCol" value="#{td.value.enabled}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18n.resourcesTable.colPool}" sort="pool" rendered="#{hasPool}"  rowHeader="$boolean{true}" id="col3">
        <sun:hyperlink id="pool" text="#{td.value.pool}" 
            url="#{editPoolLink}?name=#{td.value.pool}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18n.resourcesTable.colResType}" sort="resType" rendered="#{hasResType}"  rowHeader="$boolean{true}" id="coltype">
        <staticText id="typeCol" rendered="#{hasResType}" value="#{td.value.resType}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="$pageSession{showDetail}"
            headerText="$resource{i18n.common.TargetTab}"  
            rowHeader="$boolean{true}" id="colTarget">
            <event>
                <!beforeEncode
                    getTargetStatus(appName="#{td.value.name}", forApp="$boolean{false}",  result=>$page{listOfTargets});
                />				    
            </event>
            <foreach key="target" list="#{listOfTargets}">
		<staticText id="image" value="#{target.image}" />"&nbsp; 
                <sun:hyperlink url="#{target.targetURL}" text="#{target.target}" />
                "<br />
            </foreach>
        </sun:tableColumn> 
     
    
    <sun:tableColumn headerText="$resource{i18n.resourcesTable.colDesc}" rowHeader="$boolean{true}" id="col4">
        <staticText id="typeDesc" value="#{td.value.description}" />
    </sun:tableColumn>
    
  </sun:tableRowGroup>
  
</sun:table>
