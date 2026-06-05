import sqlite3
conn = sqlite3.connect('glimmerseed.db')
c = conn.cursor()
c.execute('PRAGMA table_info(users)')
cols = [r[1] for r in c.fetchall()]
if 'device_id' not in cols:
    c.execute('ALTER TABLE users ADD COLUMN device_id VARCHAR(255) DEFAULT NULL')
    print('Added device_id column')
else:
    print('device_id column already exists')
conn.commit()
conn.close()
print('Migration done')