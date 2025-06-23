# Email Notification Setup Guide

## Overview
The application now supports email notifications for order confirmations. This feature sends detailed order confirmation emails to customers automatically when orders are placed.

## Features
- ✅ **Automatic email sending** when orders are placed
- ✅ **Beautiful HTML email template** with order details
- ✅ **Customer's actual email address** (from checkout form)
- ✅ **Complete order information** including items, shipping address, and total
- ✅ **Professional branding** with Hajj & Umrah Store styling
- ✅ **Fallback handling** - works even if email is not configured

## Email Configuration

### 1. Gmail Setup (Recommended)
1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password:**
   - Go to Google Account Settings
   - Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"
3. **Update application.properties:**
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-16-digit-app-password
   email.from.address=your-email@gmail.com
   email.from.name=Hajj & Umrah Store
   ```

### 2. Other Email Providers
For other providers, update these settings in `application.properties`:
```properties
spring.mail.host=smtp.your-provider.com
spring.mail.port=587
spring.mail.username=your-email@your-provider.com
spring.mail.password=your-password
```

## Email Template Features

### What's Included in the Email:
- 🎯 **Order ID** (6-digit number)
- 📦 **Complete order items** with prices and quantities
- 🏠 **Detailed shipping address**
- 💰 **Total amount**
- 🚚 **Shipping information** (24 hours, 5-7 days delivery)
- 📞 **Store contact information**
- 🎨 **Professional branding**

### Email Template Location:
- **Template:** `src/main/resources/templates/email/order-confirmation.html`
- **Styling:** Inline CSS for maximum email client compatibility

## Testing Email Functionality

### 1. Without Email Configuration
- The application will work normally
- Console will show: "Email notification skipped - Email not configured"
- No errors or page disruptions

### 2. With Email Configuration
- Emails will be sent automatically when orders are placed
- Console will show: "Order confirmation email sent successfully to: customer@email.com"

## Troubleshooting

### Common Issues:
1. **"Authentication failed"** - Check your app password
2. **"Connection timeout"** - Check your internet connection
3. **"Email not configured"** - Update application.properties with correct credentials

### Debug Mode:
To enable email debugging, add this to `application.properties`:
```properties
spring.mail.properties.mail.debug=true
```

## Benefits Over SMS
- ✅ **Free** - No per-message costs
- ✅ **Detailed** - Can include full order information
- ✅ **Reliable** - Better delivery rates than SMS
- ✅ **Professional** - HTML formatting with branding
- ✅ **Immediate** - Sent instantly when order is placed

## Security Notes
- Never commit real email credentials to version control
- Use environment variables for production:
  ```properties
  spring.mail.username=${EMAIL_USERNAME}
  spring.mail.password=${EMAIL_PASSWORD}
  ```

## Next Steps
1. Configure your email credentials in `application.properties`
2. Test with a real order
3. Check your email inbox for the confirmation
4. Customize the email template if needed

The email notification system is now ready to use! 🎉 