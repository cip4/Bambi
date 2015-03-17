<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" 
  xmlns="http://www.w3.org/1999/xhtml" 
  xmlns:bambi="www.cip4.org/Bambi" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  >
  <xsl:strip-space elements="*" />
  <xsl:output method="xml" />
  <xsl:template match="/DeviceList">
    
    
    <html>
        <xsl:variable name="context" select="@Context" />
        <head>
            <!-- <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/> -->
            <meta http-equiv="X-UA-Compatible" content="IE=9" />
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <!-- Google Web Font -->
            <link href='http://fonts.googleapis.com/css?family=Roboto+Condensed:400,700|Roboto:400,500' rel='stylesheet' type='text/css' />
            <!-- Stylesheet -->
            <link href="webapp/css/styles_pc.css" rel="stylesheet" type="text/css" />
            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of select="$context" />/webapp/css/styles_pc.css</xsl:attribute>
            </link>
            <!-- Favicon -->
            <link rel="icon" type="image/x-icon">
                <xsl:attribute name="href"><xsl:value-of
                                select="$context" />/webapp/favicon.ico</xsl:attribute>
            </link>
            
            <!-- Browser Title Bar -->
            <title>Overview |<xsl:value-of select="@DeviceType" /></title>
            
            <!--<script src="js/modernizr.custom.js"></script>-->
        </head>
    
    
 
    <!-- //////////////////////////////////////////////////// -->
    
    
    
    <body>
    <div id="content-wrapper">
      
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      <!-- +++++++++  First column of Bambi  +++++++++++ -->
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      
      <!-- Root Device --> 
      <!-- This is the first column of Bambi --> 
      <!-- it shows the root controller -->
      <div class="root-controller column-grey"> 
        <!-- Name -->
        <div class="headline-wrapper root-headline">
          <h1> <xsl:value-of select="@DeviceType"></xsl:value-of> </h1>
          <h2>Root Controller</h2>
          <img height="70" alt="logo" class="logo">
          <xsl:attribute name="src"><xsl:value-of
						select="$context"></xsl:value-of>/webapp/images/logo.svg</xsl:attribute>
          </img> </div>
        <div class="root-details-wrapper"> 
          <!-- Description -->
          <xsl:apply-templates select="XMLDevice[@Root='true']"></xsl:apply-templates>
          
          
          <!-- To jump to it SEARCH: "root controller spec" for applied template --> 
        </div>
        <!-- root-details-wrapper --> 
      </div>
      
      
      
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      <!-- +++++++++  Second column of Bambi  ++++++++++ -->
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
      
      <!-- Devices --> 
      <!-- This is where the Devices are listed in the Overview --> 
      <!-- in the second Column of Bambi, after the root controller -->
      <div class="devices column-grey">
        <div class="headline-wrapper">
          <h2>Devices</h2>
        </div>
        <xsl:apply-templates select="XMLDevice[@Root='false']"></xsl:apply-templates>
        
        <!-- SEARCH: "device spec" for applied template --> 
      </div>
      
      <div id="footer"><div class="displaynone">x</div></div>
    </div>
    
    
    <!--<div id="popupwrapper">
      <div>
          <iframe id="popupframe" name="popup">Your browser doesn't support frames.</iframe>
          <div class="displaynone">x</div>
      </div>
      <button id="popupclose" class="button">
    	CLOSE
      </button>
    </div>-->
    
    
    

    
    <!-- content-wrapper -->

    </body>
    </html>
  </xsl:template>
  
  
  <!-- ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////// -->
  
  
  
    <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
    <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
    <!-- +++++++++   root controller spec  ++++++++++ -->
    <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
    <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  
  <xsl:template match="XMLDevice[@Root='true']">
    <xsl:variable name="context" select="../@Context"></xsl:variable>
    
    <!-- Controller -->
    
    <div class="box controller">
        <h3>Controller ID</h3>
        <em>
        	<xsl:value-of select="@DeviceID"></xsl:value-of>
		</em>
        <div>
            <xsl:attribute name="class">
                <xsl:value-of select="@DeviceStatus">
                </xsl:value-of> status-bar
            </xsl:attribute>
            <div class="displaynone">x</div><!-- Fix for IE: without any content it won't close a tag in the transformation -->
        </div>
    </div>
    
    
    <!-- URL -->
    <div class="box controller-url">
        <h3>Controller URL</h3>
        <em><xsl:value-of select="@DeviceURL"></xsl:value-of></em>
    </div>
    
    <!-- Queue Status -->
    <div class="box queue-status">
        <h3>Queue Status</h3>
        <xsl:value-of select="@QueueStatus"></xsl:value-of>
        <div class="status-bar" >
          <xsl:attribute name="class"><xsl:value-of select="@QueueStatus"></xsl:value-of> status-bar</xsl:attribute>
          <div class="displaynone">x</div><!-- Fix for IE: without any content it won't close a tag in the transformation -->
        </div>
    </div>
    
    <!-- Queue Status Details -->
    <div class="box running"><img class="symbol">
      <xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/webapp/images/running.svg</xsl:attribute>
      </img>
    <h3>Running</h3>
    <xsl:value-of select="@QueueRunning"></xsl:value-of>
    </div>
    
    <!-- QueueWaiting -->
    <div class="box root queue-waiting"><img class="symbol">
      <xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/webapp/images/waiting.svg</xsl:attribute>
      </img>
    <h3>Waiting</h3>
    <xsl:value-of select="@QueueWaiting"></xsl:value-of>
    </div>
    
    <!-- QueueCompleted -->
    <div class="box completed"><img class="symbol">
      <xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/webapp/images/modify.svg</xsl:attribute>
      </img>
    <h3>Completed</h3>
    <xsl:value-of select="@QueueCompleted"></xsl:value-of>
    </div>
    
    <!-- All -->
    <div class="box all">
    <h3>All</h3>
    <xsl:value-of select="@QueueAll"></xsl:value-of>
    </div>
    
    <!-- Dump -->
    <div class="box">
    <h3>Dump Enabled</h3>
    <form>
      <input type="checkbox" Name="Dump" value="true" class="checkbox">
      <xsl:if test="@Dump='true'">
        <xsl:attribute name="checked">true</xsl:attribute>
      </xsl:if>
      </input>
      <input type="hidden" name="UpdateDump" value="true" />
      <input type="submit" value="Dump" class="button"/>
    </form>
    </div>
  </xsl:template>
  
  <!-- ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////// -->
  
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- ++++++++++++++  Device Spec  ++++++++++++++++ -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  
  <xsl:template match="XMLDevice[@Root='false']">
    <xsl:variable name="context" select="../@Context" />
    <div class="fix">
      <xsl:attribute name="class"><xsl:value-of select="@DeviceID" />device</xsl:attribute>
		  
          
          <!-- Hidden Popups triggered from within the details & queue iframes -->
		  <!-- Login-Popup -->
          <div class="popupwrapper" href="#">
                <xsl:attribute name="id">popup-login-<xsl:value-of select="@DeviceID"></xsl:value-of></xsl:attribute>

                <!-- Content frame -->
                <div>
                	
                    <!-- Content -->
                    <iframe class="popupframe" >
                        <xsl:attribute name="name">popup-login-<xsl:value-of select="@DeviceID"></xsl:value-of></xsl:attribute>
                        <xsl:attribute name="src">
                            <xsl:value-of select="$context" />/login/<xsl:value-of select="@DeviceID" />
                        </xsl:attribute>
                        Your browser doesn't support frames.
                    </iframe>
                    
                    <!-- close button -->
                    <a class="popupclose" href="#">
                        <img class="buttonsymbolimage popupclosebutton">
                            <xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/webapp/images/close.svg</xsl:attribute>
                        </img>
                    </a>
                </div>
                
                                
          </div>
          
          <!-- Subscriptions-Popup -->
          <div class="popupwrapper" href="#">
             <xsl:attribute name="id">popup-subscriptions-<xsl:value-of select="@DeviceID"></xsl:value-of></xsl:attribute>
                <!-- close button 
          		<a class="popupclose" href="#">
                	<img class="buttonsymbolimage popupclosebutton">
                    	<xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/webapp/images/close.svg</xsl:attribute>
                    </img>
                </a>-->
                <!-- Content frame -->             
              <div>
                  <iframe class="popupframe" >
                      <xsl:attribute name="name">popup-subscriptions-<xsl:value-of select="@DeviceID"></xsl:value-of></xsl:attribute>
                      <xsl:attribute name="src">
                          <xsl:value-of select="$context" />/showSubscriptions/<xsl:value-of select="@DeviceID" />
                      </xsl:attribute>
                      Your browser doesn't support frames.
                  </iframe>
                  
                  <!-- close button -->
                    <a class="popupclose" href="#">
                        <img class="buttonsymbolimage popupclosebutton">
                            <xsl:attribute name="src"><xsl:value-of select="$context"></xsl:value-of>/webapp/images/close.svg</xsl:attribute>
                        </img>
                    </a>
              </div>
          </div>

      <!-- Device ID -->
      <input type="radio" class="selector" name="devices" >
      
      <!-- Hidden via CSS - Radiobutton to select single device and display details -->
      <xsl:attribute name="id"><xsl:value-of select="@DeviceID" /> selector</xsl:attribute>
      </input>
      <label class="accordeon-label">
      <!-- Label for Radiobutton to make selection with RB hidden -->
      <xsl:attribute name="for"><xsl:value-of select="@DeviceID" /> selector</xsl:attribute>
      <div class="box device-id">
      
      
      <!-- Device name -->
      <h3><xsl:value-of select="@DeviceID" /></h3>
      
      <!-- Arrow showing you can expand the device -->
      <div><xsl:attribute name="class"><xsl:value-of select="@DeviceID" /> arrowright</xsl:attribute>
      <img>
      <xsl:attribute name="src"><xsl:value-of
						select="$context" />/webapp/images/arrowright.svg</xsl:attribute></img>
      </div>
      
      <!-- Device desctiption -->
      <em><xsl:value-of select="@DeviceType" /></em>
      <div>
        <xsl:attribute name="class"><xsl:value-of select="@DeviceStatus" /> status-bar</xsl:attribute>
        <div class="displaynone">x</div><!-- Fix for IE: without any content it won't close a tag in the transformation -->
      </div>
      </div>
      </label>
      
      
      <div class="detailwrapper">
        <div>
          
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- ++++++++  Third column of Bambi  ++++++++++++ -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->         
          
          <xsl:attribute name="class"><xsl:value-of select="@DeviceID" /> device-details  column-white</xsl:attribute>
          <div class="headline-wrapper">
            <h2>Device:<xsl:value-of select="@DeviceID" /></h2>
          </div>
          

          
          <!-- Device with Link to more Detailed view-->
          
          <!-- "Button" for Expandable Details Menu with reload functions, device functions and device actions -->
          <!-- Selection handled via Radiobuttons for skipping between devices -->
          <input type="radio" name="device-functions">
            <xsl:attribute name="id"><xsl:value-of select="@DeviceID"/>-functions</xsl:attribute>
            <xsl:attribute name="class"><xsl:value-of select="@DeviceID"/> selector</xsl:attribute>
          </input>
          
          
          <!-- Open further Details of the device -->
          <!-- Clickable Label to open the Device Details -->
          <!-- When the hidden input is checked, the queue-wrapper gets visible -->
          <label>
            <xsl:attribute name="for"><xsl:value-of select="@DeviceID"/>-functions</xsl:attribute>
            <div class="box details">
            
              <!-- Arrow that points out that there is more information to open -->
              <div><xsl:attribute name="class"><xsl:value-of select="@DeviceID" /> arrowright</xsl:attribute>
                  <img>
                      <xsl:attribute name="src"><xsl:value-of select="$context" />/webapp/images/arrowright.svg</xsl:attribute></img>
              </div>
        
              <h3>Details</h3>
              <xsl:value-of select="@DeviceID" />
            </div>
          </label>
          
          
          <!-- details iframe -->
          <div class="details-frame">
              
            <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
            <!-- ++++++++++++++  Frame for the  ++++++++++++++ -->
            <!-- +++++++++  Fourth column of Bambi  ++++++++++ -->
            <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
            <!-- +++++++++++++++++++++++++++++++++++++++++++++ -->
            <iframe>
            <xsl:attribute name="class">double-column-frame details-frame details-<xsl:value-of select="@DeviceID" /></xsl:attribute>
            <xsl:attribute name="name">details-<xsl:value-of select="@DeviceID" /></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="$context" />/showDevice/<xsl:value-of select="@DeviceID" /></xsl:attribute>
            your browser doesn't support frames.
            </iframe>
            <div class="displaynone">x</div>
          </div>
          
          <!-- DeviceURL -->
          <div class="box URL">
          <h3>Device URL</h3>
          <em><xsl:value-of select="@DeviceURL" /></em>
          </div>
          
          <!-- QueueStatus -->
          <input type="radio" name="device-functions">
          <xsl:attribute name="id"><xsl:value-of select="@DeviceID"/>-queue</xsl:attribute>
          <xsl:attribute name="class"><xsl:value-of select="@DeviceID"/> selector</xsl:attribute>
          </input>
          
          <!-- Clickable Label to open the Queue Details -->
          <!-- When the hidden input is checked, the queue-wrapper gets visible -->
          <label>
          <xsl:attribute name="for"><xsl:value-of select="@DeviceID"/>-queue</xsl:attribute>

          <div class="box queue-status">
          
          
          <!-- Arrow, showing the item can be expanded -->
          <div><xsl:attribute name="class"><xsl:value-of select="@DeviceID" /> arrowright</xsl:attribute>
      			<img>
      				<xsl:attribute name="src"><xsl:value-of select="$context" />/webapp/images/arrowright.svg</xsl:attribute></img>
	   		</div>
            <!-- symbol for the queue status -->
            <img class="symbol">
            <xsl:attribute name="src"><xsl:value-of select="$context" />/webapp/images/queue-status.svg</xsl:attribute>
            </img>
            <div class="openexternal">
            <a target="_new">
            <xsl:attribute name="href">
            	<xsl:value-of select="$context"/>/showQueue/<xsl:value-of select="@DeviceID" />
            </xsl:attribute>
            <em class="smaller">Open in new Window</em></a>
            </div>
          
          <!-- Headline -->
          <h3>Queue Status</h3>
          <!-- Status -->
          <xsl:value-of select="@QueueStatus" />
          </div>
          <!--</a>-->
          </label>
          
          <!-- div with iframe that shows the expanded version of the queue status -->
          <!-- the page in the frame is loaded at the time bambi loads -->
          <div class="queue-frame double-column-frame">
            <iframe>
            <xsl:attribute name="class">double-column-frame queue-frame queue-<xsl:value-of select="@DeviceID" /></xsl:attribute>
            <xsl:attribute name="name">queue-<xsl:value-of select="@DeviceID" /></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="$context" />/showQueue/<xsl:value-of select="@DeviceID" /></xsl:attribute>
            Your browser doesn't support frames.
            </iframe>
            <div class="displaynone">x</div>
          </div>
          
          <!-- QueueRunning -->
          <div class="box running"><img class="symbol">
            <xsl:attribute name="src"><xsl:value-of select="$context" />/webapp/images/running.svg</xsl:attribute>
            </img>
          <h3>Running</h3>
          <xsl:value-of select="@QueueRunning" />
          </div>
          
          <!-- QueueWaiting -->
          <div class="box waiting"><img class="symbol">
            <xsl:attribute name="src"><xsl:value-of select="$context" />/webapp/images/waiting.svg</xsl:attribute>
            </img>
          <h3>Waiting</h3>
          <xsl:value-of select="@QueueWaiting" />
          </div>
          
          <!-- QueueCompleted -->
          <div class="box completed"><img class="symbol">
            <xsl:attribute name="src"><xsl:value-of select="$context" />/webapp/images/modify.svg</xsl:attribute>
            </img>
          <h3>Completed</h3>
          <xsl:value-of select="@QueueCompleted" />
          </div>
          
          <!-- QueueAll -->
          <div class="box all">
          <h3>All</h3>
          <xsl:value-of select="@QueueAll" />
          </div>

        </div>
        <!-- .device-details -->
      </div>
      <!-- .detailwrapper -->
    </div>
    <!-- .device -->
  </xsl:template>
  <xsl:include href="CPUTimer.xsl" />
</xsl:stylesheet>
