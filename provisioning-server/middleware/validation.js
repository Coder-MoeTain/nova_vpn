/**
 * Input validation middleware using express-validator
 */

const { body, param, query, validationResult } = require('express-validator');

/**
 * Validates WireGuard public key format (base64, 44 chars)
 */
const publicKeyValidator = (field = 'publicKey') => {
  return body(field)
    .trim()
    .notEmpty()
    .withMessage(`${field} is required`)
    .isLength({ min: 44, max: 44 })
    .withMessage(`${field} must be exactly 44 characters`)
    .matches(/^[A-Za-z0-9+/]{43}=$/)
    .withMessage(`${field} must be a valid base64 WireGuard public key`);
};

/**
 * Validates device name (optional, max 128 chars, alphanumeric + spaces/hyphens)
 */
const deviceNameValidator = () => {
  return body('deviceName')
    .optional()
    .trim()
    .isLength({ max: 128 })
    .withMessage('deviceName must be at most 128 characters')
    .matches(/^[a-zA-Z0-9\s\-_.]+$/)
    .withMessage('deviceName can only contain letters, numbers, spaces, hyphens, underscores, and periods')
    .escape();
};

/**
 * Validates device ID from app (optional, max 64 chars, e.g. Android ID)
 */
const deviceIdValidator = () => {
  return body('deviceId')
    .optional()
    .trim()
    .isLength({ max: 64 })
    .withMessage('deviceId must be at most 64 characters')
    .escape();
};

/**
 * Validates app version string (optional, max 64 chars)
 */
const appVersionValidator = () => {
  return body('appVersion')
    .optional()
    .trim()
    .isLength({ max: 64 })
    .withMessage('appVersion must be at most 64 characters')
    .escape();
};

/**
 * Validates hostname (optional, max 64 chars)
 */
const hostnameValidator = () => {
  return body('hostname')
    .optional()
    .trim()
    .isLength({ max: 64 })
    .withMessage('hostname must be at most 64 characters')
    .matches(/^[a-zA-Z0-9.\-_]+$/)
    .withMessage('hostname contains invalid characters')
    .escape();
};

/**
 * Validates model string (optional, max 64 chars)
 */
const modelValidator = () => {
  return body('model')
    .optional()
    .trim()
    .isLength({ max: 64 })
    .withMessage('model must be at most 64 characters')
    .escape();
};

/**
 * Validates phone number (E.164 format)
 */
const phoneNumberValidator = () => {
  return body(['phoneNumber', 'phone'])
    .optional()
    .trim()
    .matches(/^\+?[1-9]\d{1,14}$/)
    .withMessage('phoneNumber must be in E.164 format (e.g., +1234567890)')
    .isLength({ max: 32 })
    .withMessage('phoneNumber must be at most 32 characters');
};

/**
 * Validates latitude (-90 to 90)
 */
const latitudeValidator = () => {
  return body(['latitude', 'lat'])
    .optional()
    .custom((value) => {
      const num = Number(value);
      if (isNaN(num)) {
        throw new Error('latitude must be a number');
      }
      if (num < -90 || num > 90) {
        throw new Error('latitude must be between -90 and 90');
      }
      return true;
    });
};

/**
 * Validates longitude (-180 to 180)
 */
const longitudeValidator = () => {
  return body(['longitude', 'lng'])
    .optional()
    .custom((value) => {
      const num = Number(value);
      if (isNaN(num)) {
        throw new Error('longitude must be a number');
      }
      if (num < -180 || num > 180) {
        throw new Error('longitude must be between -180 and 180');
      }
      return true;
    });
};

/**
 * Validates limit query parameter for pagination
 */
const limitValidator = () => {
  return query('limit')
    .optional()
    .isInt({ min: 1, max: 500 })
    .withMessage('limit must be between 1 and 500')
    .toInt();
};

/**
 * Validates public key in URL parameter
 */
const publicKeyParamValidator = () => {
  return param('publicKey')
    .trim()
    .notEmpty()
    .withMessage('publicKey parameter is required')
    .isLength({ min: 44, max: 44 })
    .withMessage('publicKey must be exactly 44 characters')
    .matches(/^[A-Za-z0-9+/]{43}=$/)
    .withMessage('publicKey must be a valid base64 WireGuard public key');
};

/**
 * Middleware to check validation results and return errors
 */
const validate = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      error: 'Validation failed',
      details: errors.array().map(err => ({
        field: err.path || err.param,
        message: err.msg,
        value: err.value
      }))
    });
  }
  next();
};

/**
 * Validation rules for POST /provision endpoint
 */
const validateProvision = [
  publicKeyValidator('publicKey'),
  deviceNameValidator(),
  hostnameValidator(),
  modelValidator(),
  phoneNumberValidator(),
  latitudeValidator(),
  longitudeValidator(),
  deviceIdValidator(),
  appVersionValidator(),
  validate
];

/**
 * Validation rules for POST /report-location endpoint
 */
const validateReportLocation = [
  publicKeyValidator('publicKey'),
  latitudeValidator(),
  longitudeValidator(),
  validate
];

/**
 * Validation rules for PATCH /api/peers endpoint
 */
const validateUpdatePeer = [
  publicKeyValidator('publicKey'),
  deviceNameValidator(),
  validate
];

/**
 * Validation rules for DELETE /api/peers endpoint
 */
const validateDeletePeer = [
  body('publicKey')
    .optional()
    .trim()
    .isLength({ min: 44, max: 44 })
    .matches(/^[A-Za-z0-9+/]{43}=$/),
  query('publicKey')
    .optional()
    .trim()
    .isLength({ min: 44, max: 44 })
    .matches(/^[A-Za-z0-9+/]{43}=$/),
  body('publicKey').custom((value, { req }) => {
    if (!value && !req.query?.publicKey) {
      throw new Error('publicKey is required in body or query');
    }
    return true;
  }),
  validate
];

/**
 * Validation rules for POST /api/peers/ban endpoint
 */
const validateBanPeer = [
  publicKeyValidator('publicKey'),
  validate
];

/**
 * Validation rules for GET /api/peers/:publicKey/location-history endpoint
 */
const validateLocationHistory = [
  publicKeyParamValidator(),
  limitValidator(),
  validate
];

module.exports = {
  validateProvision,
  validateReportLocation,
  validateUpdatePeer,
  validateDeletePeer,
  validateBanPeer,
  validateLocationHistory,
  validate,
};
