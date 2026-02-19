/**
 * Migration script to add banned columns to existing database
 * Run: node scripts/add-banned-columns.js
 */

const path = require('path');
const fs = require('fs');
const rootEnv = path.join(__dirname, '..', '..', '.env');
const localEnv = path.join(__dirname, '..', '.env');
if (fs.existsSync(rootEnv)) require('dotenv').config({ path: rootEnv });
else require('dotenv').config({ path: localEnv });

const db = require('../db');

async function addBannedColumns() {
  if (!db.isEnabled()) {
    console.log('MySQL is not enabled. Ban functionality requires MySQL.');
    process.exit(0);
  }

  try {
    console.log('Checking for banned columns...');
    await db.ensureSchema();
    
    const pool = await db.getPool();
    const conn = await pool.getConnection();
    
    try {
      // Check if columns exist
      const [columns] = await conn.query(`
        SELECT COLUMN_NAME 
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'peers' 
        AND COLUMN_NAME IN ('banned', 'banned_at')
      `);
      const existingColumns = columns.map(c => c.COLUMN_NAME);
      
      if (existingColumns.includes('banned') && existingColumns.includes('banned_at')) {
        console.log('✓ Banned columns already exist');
        return;
      }
      
      // Add missing columns
      if (!existingColumns.includes('banned')) {
        console.log('Adding banned column...');
        await conn.query(`ALTER TABLE peers ADD COLUMN banned BOOLEAN NOT NULL DEFAULT FALSE`);
        await conn.query(`ALTER TABLE peers ADD INDEX idx_banned (banned)`).catch(() => {});
        console.log('✓ Added banned column');
      }
      
      if (!existingColumns.includes('banned_at')) {
        console.log('Adding banned_at column...');
        await conn.query(`ALTER TABLE peers ADD COLUMN banned_at DATETIME NULL`);
        console.log('✓ Added banned_at column');
      }
      
      console.log('Migration completed successfully!');
    } finally {
      conn.release();
    }
  } catch (err) {
    console.error('Migration failed:', err);
    process.exit(1);
  }
}

addBannedColumns().then(() => {
  process.exit(0);
}).catch(err => {
  console.error('Error:', err);
  process.exit(1);
});
