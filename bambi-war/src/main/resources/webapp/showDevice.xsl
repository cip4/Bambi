<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:jdf="http://www.CIP4.org/JDFSchema_1_1" xmlns:bambi="www.cip4.org/Bambi" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  
  <xsl:strip-space elements="*" />
  <xsl:output method="html" />
  <xsl:template match="/XMLDevice">
  
  
  <html>
    
    <!-- -->
    
    <!--  -->
    <!-- Das ist die host:8080/SimWorker/showDevice Seite -->
    <!--  -->
    
    <!-- -->
    
    <xsl:variable name="deviceID" select="@DeviceID" />
    <xsl:variable name="deviceType" select="@DeviceType" />
    <xsl:variable name="deviceURL" select="@DeviceURL" />
    <xsl:variable name="deviceStatus" select="@DeviceStatus" />
    <xsl:variable name="context" select="@Context" />
    <xsl:variable name="modify" select="@modify" />
    
    
    <head>
    
    <meta http-equiv="X-UA-Compatible" content="IE=9" />
    <!-- Google Web Font -->
    <link href='http://fonts.googleapis.com/css?family=Roboto+Condensed:400,700|Roboto:400,500' rel='stylesheet' type='text/css' />
    <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href"><xsl:value-of select="$context" />/webapp/css/styles_pc.css</xsl:attribute>
    </link>
    
    <link rel="icon" type="image/x-icon">
    <xsl:attribute name="href"><xsl:value-of
						select="$context" />/webapp/favicon.ico</xsl:attribute>
    </link>
    
    
    <title><xsl:value-of select="$deviceType" />Simulation Device :<xsl:value-of select="$deviceID" /></title>
    
    
    <xsl:if test="@refresh='true'">
      
<!--    <script type="text/javascript">
		window.onload = setupRefresh;
	
		function setupRefresh() {
		  setTimeout("refreshPage();", 5000); // milliseconds
		}
		function refreshPage() {
		  window.location.reload();
		}
	</script>-->
      	<script type="text/javascript">
			<xsl:attribute name="src">
				<xsl:value-of select="$context" />/webapp/js/refresh.js
			</xsl:attribute>		
		</script>
      
    </xsl:if>
    
    </head><!-- Body only -->
    
    <body class="">
    <div class="content-wrapper column-grey">
      <div class="headline-wrapper">
        <h2>Details: 
          <!--<xsl:value-of select="$deviceType" />
					:--> 
          <xsl:value-of select="$deviceID" /> </h2>
      </div>
   
      
      
      <!-- device info section -->
      
      <!-- Device Details --> 
      <!-- Modifying-Page -->
      <xsl:if test="$modify='true'">
        <xsl:call-template name="devicedetails" />
        
      </xsl:if>
      
      <!-- Status Page -->
      <xsl:if test="$modify!='true'">
        <xsl:call-template name="devicedetails" />
        
      </xsl:if>
      
      <!-- Employees 
      <xsl:if test="jdf:Employee">
        <h3>Employess currently logged into this device</h3>
        <table>
          <tbody>
            <tr>
              <th>Employee ID</th>
              <th>Name</th>
              <th>Roles</th>
            </tr>
            <xsl:for-each select="jdf:Employee">
              <xsl:call-template name="showEmployee" />
              
            </xsl:for-each>
          </tbody>
        </table>
        <hr />
      </xsl:if> -->
      
      <div class="column-white column-5">
      <div class="headline-wrapper"></div>
         <div class="reload-functions box noheight"> 
     <h3>Device functions</h3>
        <!-- Reload functions -->
        
        
        <xsl:choose>
          
          <xsl:when test="@refresh='true'">
            
            <!-- Go back to not reloading the page continually -->
            
            <div class="reload stop">
              <form>
                <xsl:attribute name="action">
                	<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="$deviceID" />?refresh=false
                </xsl:attribute>
                <input type="hidden" name="refresh" value="false" />
                <input type="hidden" name="setup" value="true" />
                <input type="submit" class="button" value="Stop reloading (modify page)" />
              </form>
            </div><div class="buttonsymbol reload stop">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/arrowright.svg</xsl:attribute></img>
            </div>
            
          </xsl:when>
          <xsl:otherwise>
			
            <!-- Reload continually -->
            <div class="reload continually"> 
              
              <form>
                <xsl:attribute name="action">
                	<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="$deviceID" />?refresh=true
                </xsl:attribute>
                <input type="hidden" name="refresh" value="true" />
                <input type="hidden" name="setup" value="false" />
                <input type="submit" class="button" value="Reload continually" />
              </form>
            </div>
            <div class="buttonsymbol reload continually">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/refresh.svg</xsl:attribute></img>
            </div>
            
          </xsl:otherwise>
          
        </xsl:choose>
        
        
        <!-- Refresh page -->
        
        <div class="refresh-once">
          <form>
            <xsl:attribute name="action">
            	<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
            </xsl:attribute>
            <input type="hidden" name="refresh" value="false" />
            <input type="hidden" name="setup" value="true" />
            <input type="submit" class="button" value="Refresh page" />
          </form>
        </div>
        <div class="buttonsymbol refresh-once">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/refresh.svg</xsl:attribute></img>
            </div>
        <!--</div>-->
      
      <!-- reload functions --> 
       
      
      <!-- Device Actions -->

        <div class="shutdown">
          <form>
            <xsl:attribute name="action">
            	<xsl:value-of select="$context" />
            </xsl:attribute>
            <input type="hidden" name="shutdown" value="true" />
            <input type="hidden" name="setup" value="true" />
            <input type="submit" class="button" value="Shutdown" title="attention this removes the device - adding a new device is not yet implemented!" />
          </form>
        </div><div class="buttonsymbol shutdown">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/shutdown.svg</xsl:attribute></img>
            </div>
        
        <!-- Restart the Device -->
        <div class="restart">
          <form>
            <xsl:attribute name="action">
            	<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
            </xsl:attribute>
            <input type="submit" class="button" value="Restart" />
            <input type="hidden" name="setup" value="true" />
            <input type="hidden" name="restart" value="true" />
          </form>
        </div><div class="buttonsymbol restart">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/restart.svg</xsl:attribute></img>
            </div>
        
        <!-- Reset the Device -->
        <div class="reset">
          <form>
            <xsl:attribute name="action">
              <xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
            </xsl:attribute>
            <input type="submit" class="button" value="Reset"
									title="attention this is a hard reset!" />
            <input type="hidden" name="setup" value="true" />
            <input type="hidden" name="reset" value="true" />
          </form>
        </div>
        
        <div class="buttonsymbol reset">
          <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/reset.svg</xsl:attribute></img>
        </div>
        
        <!-- Modify/Edit the Device --> 
        
        <!-- Test: displayed on ststus-page, not displayed on modify-page -->
        <xsl:if test="$modify!='true'">
          
          <div class="modify">
            <form>
              <xsl:attribute name="action">
              	<xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" />
              </xsl:attribute>
              <input type="submit" class="button" value="Modify" title="update / review device details" />
              <input type="hidden" name="modify" value="true" />
              <input type="hidden" name="setup" value="true" />
              <input type="hidden" name="refresh" value="false" />
            </form>
          </div>
          <div class="buttonsymbol modify">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/modify.svg</xsl:attribute></img>
          </div>
          
        </xsl:if>
      </div><!-- actions --> 
      
      
      <!-- Device Functionality -->
      <div class="functions box noheight"> 
      
        <!-- Login Button (Log employees in) -->
        <xsl:if test="@login='true'">
          
          <!-- Button -->
          <div class="show-login">
            <form>
            <!-- Everything different to MS IE -->
            <xsl:if test="system-property('xsl:vendor') != 'Microsoft'">
              <xsl:attribute name="action">
              	<xsl:value-of select="$context"></xsl:value-of>/overview#popup-login-<xsl:value-of select="@DeviceID"></xsl:value-of>
              </xsl:attribute>
              <xsl:attribute name="target">_parent</xsl:attribute>
            </xsl:if>
              
            <!-- MS IE -->
            <xsl:if test="system-property('xsl:vendor') = 'Microsoft'">
                  
              <xsl:attribute name="action">
                  <xsl:value-of select="$context"></xsl:value-of>/login/<xsl:value-of select="@DeviceID"></xsl:value-of>
              </xsl:attribute>
              
              <xsl:attribute name="target">_blank</xsl:attribute>
              
            </xsl:if>
            
              <input type="submit" class="button" value="Login" title="open operator login screen" />
            </form>
                      <!-- Symbol -->
              <div class="buttonsymbol">
                <img class="buttonsymbolimage">
                    <xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/open.svg</xsl:attribute>
                </img>
              </div>
              
          </div>
          

          

        </xsl:if>
        
        <!-- Show Subscribtions Button -->
        <div class="show-subscriptions">
          
          <form>
            
            <!-- Everything different to MS IE -  show Popup -->
            <xsl:if test="system-property('xsl:vendor') != 'Microsoft'">
              
              <xsl:attribute name="action">
                  <xsl:value-of select="$context"></xsl:value-of>/overview#popup-subscriptions-<xsl:value-of select="@DeviceID"></xsl:value-of>
              </xsl:attribute>
              
              <xsl:attribute name="target">_parent</xsl:attribute>
              
            </xsl:if>
            
            <!-- MS IE - open new Window -->
            <xsl:if test="system-property('xsl:vendor') = 'Microsoft'">
                  
              <xsl:attribute name="action">
                  <xsl:value-of select="$context"></xsl:value-of>/showSubscriptions/<xsl:value-of select="@DeviceID"></xsl:value-of>
              </xsl:attribute>
              
              <xsl:attribute name="target">_blank</xsl:attribute>
              
            </xsl:if>
            
            
            <input type="submit" class="button" value="Show subscriptions" />
          </form>
          
          <div class="buttonsymbol">
            <img class="buttonsymbolimage"><xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/images/show.svg</xsl:attribute></img>
          </div>
          

            
            <div class="clear"></div>
        </div>

      
      </div><!-- functions --> 
      
      
      
      <!-- CPU Timer -->
      <div class="box noheight"><xsl:apply-templates />
      
      <xsl:call-template name="cputimer" /></div>
     
      
      
      <!-- Bambi Info  
      <font size="-1" color="#b0c4de">
      <table>
        <xsl:if test="@VersionString">
          <tr>
            <td>Bambi Product Version:</td>
            <td><xsl:value-of select="@VersionString" /></td>
          </tr>
        </xsl:if>
        <tr>
          <td>Bambi Internal Build: </td>
          <td>2210 at 20130227</td>
        </tr>
        <tr>
          <td>JDFLib Build:</td>
          <td>73, JDF Schema Version:
            2.1.4a</td>
        </tr>
      </table>
      </font>--> </div> </div>
    </body>
    </html>
  </xsl:template>
  
  <!-- ============================================================ -->
  
  <xsl:include href="processor.xsl" />
  <xsl:include href="modifyString.xsl" />
  <xsl:include href="DeviceExtension.xsl" />
  <xsl:include href="CPUTimer.xsl" />
  
  <!-- modifiable Employee -->
  <xsl:template name="showEmployee">
    <tr class="device">
      <td><xsl:value-of select="@ProductID" /></td>
      <td><xsl:value-of select="jdf:Person/@DescriptiveName" /></td>
      <td><xsl:value-of select="@Roles" /></td>
    </tr>
  </xsl:template>
  
  <!-- modifiable phase -->
  <xsl:template match="Phase">
    <br />
    <h2>Current Job Phase Setup</h2>
    <form>
    <xsl:attribute name="action">../processNextPhase/<xsl:value-of
				select="../@DeviceID" /></xsl:attribute>
    Device Status:
    <xsl:apply-templates select="bambi:OptionList[@name='DeviceStatus']" />
        <br />
    Device StatusDetails:
    <input name="DeviceStatusDetails" type="text" size="30"
				maxlength="30">
    <xsl:attribute name="value"><xsl:value-of
					select="@DeviceStatusDetails" /></xsl:attribute>
    </input>
    <br />
    Node Status:
    <xsl:apply-templates select="bambi:OptionList[@name='NodeStatus']" />
        <br />
    Node StatusDetails:
    <input name="NodeStatusDetails" type="text" size="30"
				maxlength="30">
    <xsl:attribute name="value"><xsl:value-of select="@NodeStatusDetails" /></xsl:attribute>
    </input>
    <br />
    Seconds to go:<xsl:value-of select="@Duration" />; new time to go:
    <input name="Duration" type="text" size="30" maxlength="30">
    <xsl:attribute name="value"></xsl:attribute>
    </input>
    <hr />
    <h3>Resource Simulation Speed Setup</h3>
    <table>
    <tr>
      <td><input type="submit" class="button" value="update phase" /></td>
      <td>
    <form>
      <xsl:attribute name="action">../showDevice/<xsl:value-of
								select="@DeviceID" /></xsl:attribute>
      <input type="hidden" name="refresh" value="false" />
      <input type="hidden" name="setup" value="true" />
      <input type="submit" class="button" value="refresh page" />
    </form>
    </td>
    </tr>
    </table>
    <xsl:apply-templates select="ResourceAmount" />
        </form>
  </xsl:template>
  <xsl:include href="optionlist.xsl" />
  <xsl:include href="DeviceDetails.xsl" />
  
  <!-- resource amount setup -->
  <xsl:template match="ResourceAmount">
    <h4><xsl:value-of select="@ResourceName" /></h4>
    <input type="hidden">
    <xsl:attribute name="name">Res<xsl:value-of
				select="@ResourceIndex" /></xsl:attribute>
    <xsl:attribute name="value"><xsl:value-of select="@ResourceName" /></xsl:attribute>
    </input>
    Waste Production:
    <input type="checkbox" value="true">
    <xsl:attribute name="name">Waste<xsl:value-of
				select="@ResourceIndex" /></xsl:attribute>
    <xsl:if test="@Waste='true'">
      <xsl:attribute name="checked">Waste</xsl:attribute>
    </xsl:if>
    </input>
    - Speed:
    <input type="text" size="10" maxlength="30">
    <xsl:attribute name="name">Speed<xsl:value-of
				select="@ResourceIndex" /></xsl:attribute>
    <xsl:attribute name="value"><xsl:value-of select="@Speed" /></xsl:attribute>
    </input>
    <br />
    <xsl:apply-templates />
  </xsl:template>
  <xsl:template match="KnownEmployees">
    <!-- nop here -->
  </xsl:template>
  <xsl:template match="jdf:Employee">
    <!-- nop here -->
  </xsl:template>
  
  <!-- add more templates -->
  <!-- the catchall -->
  <xsl:template match="*">
    <h3>Unhandled element:<xsl:value-of select="name()" /></h3>
    <xsl:apply-templates />
  </xsl:template>
</xsl:stylesheet>