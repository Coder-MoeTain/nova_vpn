/**
 * Rate limiting middleware for API endpoints
 */

const rateLimit = require('express-rate-limit');

/**
 * Rate limiter for provisioning endpoint
 * Limits: 5 requests per 15 minutes per IP
 */
const provisionLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // 5 requests per window
  message: {
    error: 'Too many provisioning requests from this IP, please try again later.',
    retryAfter: '15 minutes'
  },
  standardHeaders: true, // Return rate limit info in `RateLimit-*` headers
  legacyHeaders: false, // Disable `X-RateLimit-*` headers
});

/**
 * Rate limiter for management API endpoints
 * Limits: 100 requests per 15 minutes per IP
 */
const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // 100 requests per window
  message: {
    error: 'Too many requests from this IP, please try again later.',
    retryAfter: '15 minutes'
  },
  standardHeaders: true,
  legacyHeaders: false,
});

/**
 * Rate limiter for location reporting endpoint
 * Limits: 60 requests per minute per IP (for frequent location updates)
 */
const locationReportLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 60, // 60 requests per minute
  message: {
    error: 'Too many location reports from this IP, please slow down.',
    retryAfter: '1 minute'
  },
  standardHeaders: true,
  legacyHeaders: false,
});

/**
 * Rate limiter for health check endpoint (more lenient)
 * Limits: 30 requests per minute per IP
 */
const healthLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 30, // 30 requests per minute
  message: {
    error: 'Too many health check requests, please slow down.'
  },
  standardHeaders: true,
  legacyHeaders: false,
});

module.exports = {
  provisionLimiter,
  apiLimiter,
  locationReportLimiter,
  healthLimiter,
};
