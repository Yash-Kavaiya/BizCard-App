package com.example.util

object GoogleAppsScriptTemplate {
    val SCRIPT_CODE = """
        // Copy & paste this code into your Google Sheets Apps Script Editor (Extensions > Apps Script)
        // Click 'Deploy' > 'New deployment' > Select type: 'Web app'
        // Execute as: 'Me' | Who has access: 'Anyone'
        // Copy the Web App URL into your LeadTap app settings!

        function doPost(e) {
          try {
            var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
            var data = JSON.parse(e.postData.contents);
            var leads = Array.isArray(data) ? data : [data];
            
            // Add Header Row if sheet is blank
            if (sheet.getLastRow() === 0) {
              sheet.appendRow([
                "Full Name", "Company", "Job Title", "Primary Phone", 
                "Primary Email", "Website", "LinkedIn", "Industry", 
                "Address", "Notes", "Status", "Source", "Date Logged"
              ]);
              sheet.getRange(1, 1, 1, 13).setFontWeight("bold").setBackground("#E0F2FE");
            }
            
            leads.forEach(function(lead) {
              sheet.appendRow([
                lead.fullName || "",
                lead.company || "",
                lead.jobTitle || "",
                lead.phonePrimary || "",
                lead.emailPrimary || "",
                lead.website || "",
                lead.linkedIn || "",
                lead.industry || "",
                lead.address || "",
                lead.notes || "",
                lead.status || "NEW",
                lead.source || "SCAN",
                new Date().toLocaleString()
              ]);
            });
            
            return ContentService.createTextOutput(JSON.stringify({
              "status": "success",
              "message": "Exported " + leads.length + " lead(s) successfully"
            })).setMimeType(ContentService.MimeType.JSON);
          } catch (err) {
            return ContentService.createTextOutput(JSON.stringify({
              "status": "error",
              "message": err.toString()
            })).setMimeType(ContentService.MimeType.JSON);
          }
        }
    """.trimIndent()
}
