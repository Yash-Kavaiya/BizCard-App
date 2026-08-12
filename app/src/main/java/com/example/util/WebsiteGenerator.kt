package com.example.util

import com.example.data.model.PersonalCard

object WebsiteGenerator {

    fun generateDigitalCardHtml(card: PersonalCard): String {
        val vCardData = VCardUtils.generateVCardFromPersonalCard(card)
        val name = card.name.ifBlank { "Professional Profile" }
        val title = card.title.ifBlank { "Business Professional" }
        val company = card.company.ifBlank { "Enterprise" }
        val email = card.email
        val phone = card.phone
        val website = card.website
        val linkedIn = card.linkedIn
        val address = card.address
        val bio = card.bio.ifBlank { "Welcome to my digital business card." }

        val initials = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "BC" }

        val encodedVCard = java.net.URLEncoder.encode(vCardData, "UTF-8").replace("+", "%20")

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${name} | Digital Business Card</title>
                <style>
                    :root {
                        --bg-color: #F2F2F7;
                        --card-bg: #FFFFFF;
                        --text-primary: #000000;
                        --text-secondary: #6E6E73;
                        --accent-blue: #007AFF;
                        --accent-blue-hover: #0056B3;
                        --border-color: rgba(0, 0, 0, 0.08);
                        --button-secondary-bg: #E5E5EA;
                        --button-secondary-text: #000000;
                        --shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
                    }

                    [data-theme="dark"] {
                        --bg-color: #000000;
                        --card-bg: #1C1C1E;
                        --text-primary: #FFFFFF;
                        --text-secondary: #8E8E93;
                        --accent-blue: #0A84FF;
                        --accent-blue-hover: #409CFF;
                        --border-color: rgba(255, 255, 255, 0.12);
                        --button-secondary-bg: #2C2C2E;
                        --button-secondary-text: #FFFFFF;
                        --shadow: 0 12px 32px rgba(0, 0, 0, 0.5);
                    }

                    @media (prefers-color-scheme: dark) {
                        :root:not([data-theme="light"]) {
                            --bg-color: #000000;
                            --card-bg: #1C1C1E;
                            --text-primary: #FFFFFF;
                            --text-secondary: #8E8E93;
                            --accent-blue: #0A84FF;
                            --accent-blue-hover: #409CFF;
                            --border-color: rgba(255, 255, 255, 0.12);
                            --button-secondary-bg: #2C2C2E;
                            --button-secondary-text: #FFFFFF;
                            --shadow: 0 12px 32px rgba(0, 0, 0, 0.5);
                        }
                    }

                    * {
                        box-sizing: border-box;
                        margin: 0;
                        padding: 0;
                        font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", "SF Pro Text", "Helvetica Neue", Helvetica, Arial, sans-serif;
                        -webkit-tap-highlight-color: transparent;
                    }

                    body {
                        background-color: var(--bg-color);
                        color: var(--text-primary);
                        min-height: 100vh;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        padding: 24px 16px;
                        transition: background-color 0.3s ease, color 0.3s ease;
                    }

                    .container {
                        width: 100%;
                        max-width: 440px;
                    }

                    .theme-bar {
                        display: flex;
                        justify-content: flex-end;
                        margin-bottom: 16px;
                    }

                    .theme-toggle {
                        background: var(--card-bg);
                        color: var(--text-primary);
                        border: 1px solid var(--border-color);
                        padding: 8px 16px;
                        border-radius: 20px;
                        font-size: 13px;
                        font-weight: 600;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
                        transition: all 0.2s ease;
                    }

                    .card {
                        background-color: var(--card-bg);
                        border: 1px solid var(--border-color);
                        border-radius: 28px;
                        padding: 32px 24px;
                        box-shadow: var(--shadow);
                        text-align: center;
                        transition: background-color 0.3s ease, border-color 0.3s ease;
                    }

                    .avatar-badge {
                        width: 80px;
                        height: 80px;
                        background: linear-gradient(135deg, var(--accent-blue), #5856D6);
                        color: #FFFFFF;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 28px;
                        font-weight: 700;
                        margin: 0 auto 18px auto;
                        box-shadow: 0 6px 16px rgba(0, 122, 255, 0.25);
                    }

                    .name {
                        font-size: 24px;
                        font-weight: 700;
                        letter-spacing: -0.5px;
                        margin-bottom: 4px;
                    }

                    .title {
                        font-size: 15px;
                        color: var(--text-secondary);
                        font-weight: 500;
                        margin-bottom: 2px;
                    }

                    .company {
                        font-size: 15px;
                        color: var(--accent-blue);
                        font-weight: 600;
                        margin-bottom: 16px;
                    }

                    .bio {
                        font-size: 14px;
                        color: var(--text-secondary);
                        line-height: 1.5;
                        margin-bottom: 24px;
                        padding: 0 8px;
                    }

                    .action-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 12px;
                        margin-bottom: 24px;
                    }

                    .btn {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        padding: 12px 16px;
                        border-radius: 14px;
                        text-decoration: none;
                        font-size: 14px;
                        font-weight: 600;
                        transition: transform 0.15s ease, opacity 0.15s ease;
                    }

                    .btn:active {
                        transform: scale(0.97);
                    }

                    .btn-primary {
                        background-color: var(--accent-blue);
                        color: #FFFFFF;
                        grid-column: span 2;
                        font-size: 15px;
                        padding: 14px 20px;
                        border-radius: 16px;
                    }

                    .btn-secondary {
                        background-color: var(--button-secondary-bg);
                        color: var(--button-secondary-text);
                    }

                    .details-list {
                        text-align: left;
                        border-top: 1px solid var(--border-color);
                        padding-top: 20px;
                        margin-top: 20px;
                        display: flex;
                        flex-direction: column;
                        gap: 12px;
                    }

                    .detail-item {
                        display: flex;
                        flex-direction: column;
                        gap: 2px;
                    }

                    .detail-label {
                        font-size: 12px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        color: var(--text-secondary);
                        font-weight: 600;
                    }

                    .detail-value {
                        font-size: 15px;
                        color: var(--text-primary);
                        font-weight: 500;
                        text-decoration: none;
                        word-break: break-all;
                    }

                    footer {
                        margin-top: 24px;
                        text-align: center;
                        font-size: 12px;
                        color: var(--text-secondary);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="theme-bar">
                        <button class="theme-toggle" id="themeToggle" onclick="toggleTheme()">
                            <span id="themeIcon">🌙</span> <span id="themeLabel">Dark Mode</span>
                        </button>
                    </div>

                    <div class="card">
                        <div class="avatar-badge">${initials}</div>
                        <h1 class="name">${name}</h1>
                        <div class="title">${title}</div>
                        <div class="company">${company}</div>
                        <p class="bio">${bio}</p>

                        <div class="action-grid">
                            <a href="data:text/vcard;charset=utf-8,${encodedVCard}" download="${name.replace(" ", "_")}.vcf" class="btn btn-primary">
                                📥 Save Contact Card
                            </a>
                            ${if (phone.isNotBlank()) "<a href='tel:${phone}' class='btn btn-secondary'>📞 Call</a>" else ""}
                            ${if (email.isNotBlank()) "<a href='mailto:${email}' class='btn btn-secondary'>✉️ Email</a>" else ""}
                            ${if (website.isNotBlank()) "<a href='${website}' target='_blank' class='btn btn-secondary'>🌐 Website</a>" else ""}
                            ${if (linkedIn.isNotBlank()) "<a href='https://${linkedIn.replace("https://", "")}' target='_blank' class='btn btn-secondary'>💼 LinkedIn</a>" else ""}
                        </div>

                        <div class="details-list">
                            ${if (phone.isNotBlank()) "<div class='detail-item'><span class='detail-label'>Phone</span><a href='tel:${phone}' class='detail-value'>${phone}</a></div>" else ""}
                            ${if (email.isNotBlank()) "<div class='detail-item'><span class='detail-label'>Email</span><a href='mailto:${email}' class='detail-value'>${email}</a></div>" else ""}
                            ${if (address.isNotBlank()) "<div class='detail-item'><span class='detail-label'>Location</span><span class='detail-value'>${address}</span></div>" else ""}
                        </div>
                    </div>

                    <footer>Powered by BizCard AI Digital Cards</footer>
                </div>

                <script>
                    function toggleTheme() {
                        const html = document.documentElement;
                        const currentTheme = html.getAttribute('data-theme');
                        let newTheme = 'dark';
                        
                        if (currentTheme === 'dark') {
                            newTheme = 'light';
                        } else if (!currentTheme && window.matchMedia('(prefers-color-scheme: dark)').matches) {
                            newTheme = 'light';
                        }
                        
                        html.setAttribute('data-theme', newTheme);
                        updateButton(newTheme);
                    }

                    function updateButton(theme) {
                        const icon = document.getElementById('themeIcon');
                        const label = document.getElementById('themeLabel');
                        if (theme === 'dark') {
                            icon.textContent = '☀️';
                            label.textContent = 'Light Mode';
                        } else {
                            icon.textContent = '🌙';
                            label.textContent = 'Dark Mode';
                        }
                    }

                    // Auto-sync initial state
                    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
                        updateButton('dark');
                    } else {
                        updateButton('light');
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
