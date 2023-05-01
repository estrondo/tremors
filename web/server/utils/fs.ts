import { readFile } from 'node:fs/promises'
import { parse } from 'yaml'


export async function loadYAML<T>(path: string): Promise<T> {
  const content = await readFile(path, { encoding: 'utf8' })
  return parse(content)
}