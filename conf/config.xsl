<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="configuration">
        <html>
            <body>
                <h2>Web Veins' Configuration</h2>
                <table border="1">
                    <tr>
                        <th>name</th>
                        <th>value</th>
                    </tr>
                    <xsl:for-each select="property">
                        <tr>
                            <td><xsl:value-of select="name"/></td>
                            <td><xsl:value-of select="value"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>