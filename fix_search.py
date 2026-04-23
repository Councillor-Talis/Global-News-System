path = r'd:/code/global-news-system/frontend/src/views/Search.vue'

with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Fix line 46 (index 45) - replace the double quotes inside the template literal with single quotes
old_line = lines[45]
print('Before:', repr(old_line))

# Replace inner " " around ${lastKeyword} with single quotes
new_line = '          :description="`没有找到与\'${lastKeyword}\'相关的新闻`"\r\n'
lines[45] = new_line

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print('After:', repr(lines[45]))
print('Fixed successfully!')
