import md5 from 'crypto-js/md5'

type AccountInfo = {
  name?: string,
  email?: string,
  avatar?: string
}

export default defineEventHandler<AccountInfo>(async (event) => {
  const session = await sessionManager.get(event, current => openIdService.refresh(current))

  if (session) {
    console.debug('Getting account for email %s.', session.user?.email)
    const account = await accountService.check(session, true)

    return {
      name: account.name,
      email: account.email,
      avatar: account.email ? `https://www.gravatar.com/avatar/${md5(account.email)}?s=128` : undefined
    }
  } else {
    console.warn('There is no auth information!')
    return {}
  }
})