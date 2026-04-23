path = r'd:/code/global-news-system/frontend/src/views/Search.vue'

with open(path, 'rb') as f:
    raw = f.read()

# Fix double \r\r caused by previous fix, normalize to \r\n
fixed = raw.replace(b'\r\r\n', b'\r\n').replace(b'\r\r', b'\r\n')

with open(path, 'wb') as f:
    f.write(fixed)

print('Line endings fixed.')

# Verify line 46
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()
print('Line 46 now:', repr(lines[45]))
