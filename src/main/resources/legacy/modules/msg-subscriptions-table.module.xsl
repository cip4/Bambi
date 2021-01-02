<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:bambi="www.cip4.org/Bambi" >
    <xsl:template name="msg-subscriptions-table">
        <table class="table">
            <thead class="thead-light">
                <tr>
                    <th>Channel ID</th>
                    <th>Signal Type</th>
                    <th>Subscription Details</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="MsgSubscription">
                    <tr>
                        <xsl:choose>
                            <xsl:when test="@LastTime=' - '">
                                <xsl:choose>
                                    <xsl:when test="@Sent='0'">
                                        <xsl:attribute name="class">table-danger</xsl:attribute>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="class">table-warning</xsl:attribute>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="class">table-success</xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                        <td>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />?DetailID=<xsl:value-of select="@ChannelID" />
                                </xsl:attribute>
                                <xsl:value-of select="@ChannelID" />
                            </a>

                            <table class="table table-borderless table-sm small mt-2">
                                <tbody>
                                    <tr>
                                        <th class="pl-0">Device ID:</th>
                                        <td><xsl:value-of select="@DeviceID" /></td>
                                    </tr>
                                    <tr>
                                        <th class="pl-0">QueueEntry ID:</th>
                                        <td><xsl:value-of select="@QueueEntryID" /></td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                        <td>
                            <xsl:value-of select="@Type" />
                        </td>
                        <td>
                            <table class="table table-borderless table-sm table-hover small">
                                <tbody>
                                    <tr>
                                        <th>Subscription Url:</th>
                                        <td><xsl:value-of select="@URL" /></td>
                                    </tr>
                                    <tr>
                                        <th>Repeat-Time:</th>
                                        <td><xsl:value-of select="@RepeatTime" /></td>
                                    </tr>
                                    <tr>
                                        <th>Repeat-Step:</th>
                                        <td><xsl:value-of select="@RepeatStep" /></td>
                                    </tr>
                                    <tr>
                                        <th>Messages Queued:</th>
                                        <td><xsl:value-of select="@Sent" /></td>
                                    </tr>
                                    <tr>
                                        <th>Last time Queued:</th>
                                        <td><xsl:value-of select="@LastTime" /></td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                        <td class="text-right">
                            <form class="mb-0">
                                <xsl:attribute name="action">
                                    <xsl:value-of select="../@Context" />/showSubscriptions/<xsl:value-of select="../@DeviceID" />
                                </xsl:attribute>
                                <input type="hidden" name="StopChannel" value="true" />
                                <input type="hidden" name="ChannelID">
                                    <xsl:attribute name="value"><xsl:value-of select="@ChannelID" /></xsl:attribute>
                                </input>
                                <input type="submit" class="btn btn-outline-danger" value="Remove" />
                            </form>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
        <xsl:if test="not(MsgSubscription)">
            <p><i>No subscriptions are set up for this device.</i></p>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
