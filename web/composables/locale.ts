export function useLocalTemplate() {
  const { locale } = useI18n()

  return (messages: Record<string, string> | string): string => {
    return (typeof messages !== 'string') ? messages[locale.value] : messages
  }
}