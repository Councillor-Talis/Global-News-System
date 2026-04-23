import sys

path = r'd:/code/global-news-system/frontend/src/views/Search.vue'

# Read raw bytes to check BOM or encoding
with open(path, 'rb') as f:
    raw = f.read()

# Check BOM
if raw[:3] == b'\xef\xbb\xbf':
    print('BOM detected: UTF-8 with BOM')
    encoding = 'utf-8-sig'
elif raw[:2] == b'\xff\xfe':
    print('BOM detected: UTF-16 LE')
    encoding = 'utf-16'
elif raw[:2] == b'\xfe\xff':
    print('BOM detected: UTF-16 BE')
    encoding = 'utf-16'
else:
    print('No BOM, trying utf-8')
    encoding = 'utf-8'

print('First 10 bytes:', raw[:10].hex())

# Check line 46 raw bytes
lines_raw = raw.split(b'\n')
print('Line 46 raw hex:', lines_raw[45].hex())
print('Line 46 raw bytes:', lines_raw[45])
