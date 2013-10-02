<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" 
xmlns:bambi="www.cip4.org/Bambi" 
xmlns:xjdf="http://www.CIP4.org/JDFSchema_2_0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:strip-space elements="*"/>
  <xsl:output method="html"/>
  <xsl:template match="/SubscriptionList">
    <xsl:variable name="context" select="@Context"/>
    <xsl:variable name="deviceID" select="@DeviceID"/>
    <html>
      <head>
       <!-- Google Web Font -->
    <link href='http://fonts.googleapis.com/css?family=Roboto+Condensed:400,700|Roboto:400,500' rel='stylesheet' type='text/css' />
    <!-- Stylesheet -->
    <link href="css/styles_pc.css" rel="stylesheet" type="text/css" />
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <link rel="stylesheet" type="text/css" href="../css/styles_pc.css"/>
        <link rel="icon" href="favicon.ico" type="image/x-icon"/>
        
        
        <title>
          <xsl:value-of select="@DeviceID"/>
          - Subscriptions
        </title>
        
        
      </head>
      <body class="popupcontent">
        
        
        <div class="headline-wrapper">
          <!-- Headline -->
          <h2>
            <xsl:value-of select="@DeviceID"/>
            - Subscriptions
          </h2>
          

          
          <!-- refresh subscription List -->
          <a>
            <xsl:attribute name="href"><xsl:value-of select="@Context"/>/showSubscriptions/<xsl:value-of select="@DeviceID"/>
            </xsl:attribute>
            <div class="buttonsymbol">
            	<img class="buttonsymbolimage">
            		<xsl:attribute name="src"><xsl:value-of select="$context" />/images/refresh.svg</xsl:attribute>
            	</img>
            </div>
          </a>
          
          
          
        </div>
        
        <div class="box noheight">
          <!-- Subscription counter -->
          <xsl:if test="MsgSubscription">
            <h2>
              Subscriptions:
              <xsl:value-of select="count(MsgSubscription)"/>
            </h2>
          
            
          
          </xsl:if></div>
        <!-- SUBSCRIPTIONS -->
        <div class="box  nowidth clear">
            <xsl:apply-templates select="MsgSubscription"/>
            <div class="clear"></div>
        </div>

		
        
        <!-- Message Sender Headline -->
        <div class="box noheight">
        <h2>Message Sender Channels</h2>
        </div>
         
        <!-- MESSAGE SENDER CHANNELS -->
		<xsl:apply-templates select="MessageSender"/>

        
        <ul>
          <xsl:apply-templates select="RemovedChannel"/>
        </ul>

        <xsl:if test="MessageSender/Message">
        
				<xsl:call-template name="cputimer" />
		
        
          <h2>Queued Messages</h2>
          <table cellspacing="2" border="1">
            <tr>
              <th align="left"> JMF ID</th>
              <th align="left"> Sent Time</th>
              <th align="left"> Processing Status</th>
              <th align="left"> Full URL</th>
            </tr>
            <xsl:apply-templates select="MessageSender/Message"/>
          </table>

          
        </xsl:if>

        
        <xsl:apply-templates select="ProxySubscriptions"/>

      </body>
    </html>
  </xsl:template>

  <!--  end of template SubscriptionList  -->
  
  
  
  
  <xsl:template match="MsgSubscription">

       <div class="preview-wrapper" >
        <div class="subrow">
        <h3><xsl:value-of select="position()"/> - Channel ID</h3><em>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/>?DetailID=<xsl:value-of
            select="@ChannelID"/>
                  </xsl:attribute>
          <xsl:value-of select="@ChannelID"/>
        </a></em>
        
        <div>
        	<xsl:choose>
                <xsl:when test="@LastTime=' - '">
                  
                  <xsl:choose>
                    <xsl:when test="@Sent='0'">
                      <xsl:attribute name="class">status-bar red</xsl:attribute>
                    </xsl:when>
                    
                    <xsl:otherwise>
                      <xsl:attribute name="class">status-bar orange</xsl:attribute>
                    </xsl:otherwise>
                  
                  </xsl:choose>
                </xsl:when>
                
                <xsl:otherwise>
                  <xsl:attribute name="class">status-bar green</xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
        </div>
        
        </div><!--subrow-->
      </div><!-- preview-wrapper -->
      
      <!-- selector -->
      <!--<input type="radio" class="selector">
      
      <xsl:attribute name="name">
      subscriptionDetails-<xsl:value-of select="@ChannelID"></xsl:value-of>
      </xsl:attribute>
      
      <xsl:attribute name="id">
      open-<xsl:value-of select="@ChannelID"></xsl:value-of>
      </xsl:attribute>
      
      </input>
      <label class="subscriptionDetails">
      <xsl:attribute name="for">
      open-<xsl:value-of select="@ChannelID"></xsl:value-of>
      </xsl:attribute>
      
      
      <div class="open">
          <img class="center verticalopen">
          <xsl:attribute name="src"><xsl:value-of select="$context" />/images/open.svg</xsl:attribute>
          </img>
      </div>
      </label> end of subscriptionDetails -->
      
      
      
      <!-- end of selector -->
     <div class="subscriptionDetails">
     <div class="subrow"> 
      <h3>Device ID</h3><em>
        <xsl:value-of select="@DeviceID"/></em>
      </div>
      
      
      <div class="subrow">
      <h3>QueueEntry ID</h3><em>
        <xsl:value-of select="@QueueEntryID"/></em>
      </div>
      
      
      <div class="subrow">
      <h3>Signal Type</h3><em>
        <xsl:value-of select="@Type"/></em>
      </div>
      
      
      <div class="subrow">
      <h3>Subscription URL</h3><em>
        <xsl:value-of select="@URL"/></em>
      </div>
      
      
      <div class="subrow">
      <h3>Repeat Time</h3><em>
        <xsl:value-of select="@RepeatTime"/></em>
     </div>
     
     
      <div class="subrow"> 
     	<h3> Repeat Step</h3><em>
        <xsl:value-of select="@RepeatStep"/></em>
      </div>
      
      
      <div class="subrow">
    <h3>Messages Queued</h3><em>
        <xsl:value-of select="@Sent"/></em>
      </div>
      
      
      <div class="subrow">
      <h3>Last time Queued</h3><em>
        <xsl:value-of select="@LastTime"/></em>
      </div>
      
      
      <div class="subrow">
      <h3>Remove Subscription</h3><em>
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="StopChannel" value="true"/>
          <input type="hidden" name="ChannelID">
            <xsl:attribute name="value"><xsl:value-of select="@ChannelID"/></xsl:attribute>
          </input>
          <input type="submit" value="remove" class="button"/>
        </form></em>
      </div>
      </div><!-- end of subscriptionDetails -->
      
      
      <!-- close -->
      <!--<input type="radio" class="selector" checked="checked">
          <xsl:attribute name="name">
          subscriptionDetails-<xsl:value-of select="@ChannelID"></xsl:value-of>
          </xsl:attribute>
          
          <xsl:attribute name="id">
          close-<xsl:value-of select="@ChannelID"></xsl:value-of>
          </xsl:attribute>
      </input>
      
      <label class="subscriptionDetails">
          <xsl:attribute name="for">
          		close-<xsl:value-of select="@ChannelID"></xsl:value-of>
          </xsl:attribute>
          
          
          
          <div class="close">
              <img class="center verticalclose">
              <xsl:attribute name="src"><xsl:value-of select="$context" />/images/close.svg</xsl:attribute>
              </img>
          </div>
      </label>
       end of close -->
      
      
      <div class="clear"/>
      

  </xsl:template>
  
  
  
  
  <!--  end of template MsgSubscription  -->

  
  
  
  
  <xsl:template match="MessageSender">
  
  		 
         <div class="box clear MessageSender">
         <div class="subrow">

    	<!-- Base URL -->
        <h3>Base URL</h3>
        
        <xsl:value-of select="@URL"/>

      
        <!-- Status -->
        
        
        <xsl:choose>
          <xsl:when test="@Active='false'">
            down
          </xsl:when>
          <xsl:when test="@pause='true'">
            paused
          </xsl:when>
          <xsl:when test="@Size!='0'">
            <xsl:choose>
              <xsl:when test="@idle!='0'">
                dispatch errors
              </xsl:when>
              <xsl:otherwise>
                back log
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            active
          </xsl:otherwise>
        </xsl:choose>
        
      <!-- Status Bar -->
      <div class="status-bar">
      <xsl:choose>
        <!-- something is waiting -->
        <xsl:when test="@Size!='0'">
          <xsl:choose>
            <xsl:when test="@pause='true'">
              <xsl:attribute name="bgcolor">#ffccaa</xsl:attribute>
            </xsl:when>
            <xsl:when test="@Active='true'">
              <xsl:attribute name="bgcolor">#ffffaa</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="@pause='true'">
          <xsl:attribute name="bgcolor">#ffccaa</xsl:attribute>
        </xsl:when>
        <xsl:when test="@Active='true'">
         <xsl:choose>
              <xsl:when test="@Problems='true'">
          <xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
          <xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="bgcolor">#aaaaff</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      </div><!-- statusbar -->
     </div><!-- subrow -->

      
      <div class="subrow">
      <h3>JMF</h3>
      <!-- JMF Pending -->

        
        <div class="float-left">
        <em class="small"><xsl:value-of select="@Size"/> Pending</em><br/>
      
      
      <!-- JMF Sent -->

        <em class="small"><xsl:value-of select="@NumSent"/> Sent</em><br/>
      </div>
      <div class="float-left">
      <!-- JMF Queued -->

        <em class="small"><xsl:value-of select="@NumTry"/> Queued</em><br/>
      
      
      <!-- JMF Removed -->

        <em class="small"><xsl:value-of select="@NumRemove"/> Removed</em><br/>
       </div> 
      </div>
      
      <!-- Fire & Forget Removed -->
         <div class="subrow">
        <h3>Fire and Forget Removed</h3>
        
        <em><xsl:value-of select="@NumRemoveFireForget"/></em>
      </div>
      
      <!-- JMF Errors Removed -->
         <div class="subrow">
        <h3>JMF Errors Removed</h3>
        
        <em><xsl:value-of select="@NumRemoveError"/></em>
      </div>
      
      <!-- Last time Sent -->
         <div class="subrow">
        <h3>Last time Sent</h3>
        
        <em><xsl:value-of select="@LastSent"/></em>
      </div>
      
      <!-- Last time Queued -->
         <div class="subrow">
        <h3>Last time Queued</h3>
        
        <em><xsl:value-of select="@LastQueued"/></em>
      </div>
      
      <!-- Active since -->
         <div class="subrow">
        <h3>Active since</h3>
        
        <em><xsl:value-of select="@CreationDate"/></em>
      </div>
      
      <!-- Show Sent Messages -->
         
         
         <div class="subrow">
        <h3>Show Sent Messages</h3>
        
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="ListSenders" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="submit" value="List Senders" class="button"/>
        </form>
      </div>
      
      <!-- pause / resume -->
       <div class="subrow">  
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <xsl:choose>
            <xsl:when test="@pause='true'">
              <input type="hidden" name="pause" value="false"/>
              <input type="submit" value="resume" class="button"/>
            </xsl:when>
            <xsl:otherwise>
              <input type="hidden" name="pause" value="true"/>
              <input type="submit" value="pause" class="button"/>
            </xsl:otherwise>
          </xsl:choose>
        </form>
      
      
      <!-- Remove Sender -->
         
        <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="StopSender" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="submit" value="remove" class="button"/>
        </form>
      
      
      <!-- Flush unsent Messages -->
         
         <form>
          <xsl:attribute name="action"><xsl:value-of select="../@Context"/>/showSubscriptions/<xsl:value-of select="../@DeviceID"/></xsl:attribute>
          <input type="hidden" name="FlushSender" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="submit" value="flush" class="button"/>
        </form>
        </div>
        
     </div><!-- row -->

  </xsl:template>
  <!--  end of template MessageSender  -->
  
  
  
  
  <xsl:template match="RemovedChannel">
    <li>
      Subscription
      <xsl:value-of select="@ChannelID"/>
      to
      <xsl:value-of select="@URL"/>
      has been removed.
    </li>
  </xsl:template>
  <!--  end of template RemovedChannel  -->
  <xsl:template match="Message">
    <tr>
        <xsl:choose>
        <!-- something is waiting -->
         <xsl:when test="@Return='sent'">
          <xsl:attribute name="bgcolor">#aaffaa</xsl:attribute>
        </xsl:when>
         <xsl:when test="@Return='error'">
          <xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
        </xsl:when>
         <xsl:when test="@Return='removed'">
          <xsl:attribute name="bgcolor">#ffaaaa</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="bgcolor">#aaaaaa</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <td>
        <xsl:value-of select="position()"/>
        <form>
          <xsl:attribute name="action"><xsl:value-of select="/SubscriptionList/@Context"/>/showSubscriptions/<xsl:value-of select="/SubscriptionList/@DeviceID"/></xsl:attribute>
          <input type="hidden" name="ListSenders" value="true"/>
          <input type="hidden" name="URL">
            <xsl:attribute name="value"><xsl:value-of select="@URL"/></xsl:attribute>
          </input>
          <input type="hidden" name="pos">
            <xsl:attribute name="value"><xsl:value-of select="position()"/></xsl:attribute>
          </input>
          <input type="submit" value="Show Details"/>
        </form>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="xjdf:JMF/@TimeStamp">
            <xsl:value-of select="xjdf:JMF/@TimeStamp"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@TimeStamp"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
        <xsl:value-of select="@Return"/>
      </td>
     <td>
        <xsl:value-of select="@URL"/>
      </td>
    </tr>
  </xsl:template>
  



  
  
  <xsl:include href="SubscriptionExtension.xsl"/>
	<xsl:include href="CPUTimer.xsl" />

</xsl:stylesheet>