import { Account, Account__Output } from "./@types/grpc/webapi/Account"
import { AccountReponse, AccountReponse__Output } from "./@types/grpc/webapi/AccountReponse"
import * as grpc from '@grpc/grpc-js'
import { AuthSession } from "./types"
import { AccountServiceClient } from "./@types/grpc/webapi/AccountService"
import { buildMetadataFromAuthSession } from "./grpc"
import { AccountUpdate } from "./@types/grpc/webapi/AccountUpdate"

const noEmailAssociated = () => new Error('There is no e-mail associated!')
const buildMetadata = buildMetadataFromAuthSession

class AccountService {

  #client: AccountServiceClient

  constructor(client: AccountServiceClient) {
    this.#client = client
  }

  async check(session: AuthSession, create: boolean = false): Promise<Account> {
    try {
      return await this.get(session)
    } catch (error: any) {
      if (create && error.code === grpc.status.NOT_FOUND) {
        console.warn('Trying to create an account for %s.', session.user?.email)
        await this.create(session)
        console.debug('Account for %s was created.', session.user?.email)
        return this.get(session)
      } else {
        throw error
      }
    }
  }

  async create(session: AuthSession): Promise<AccountReponse> {
    if (session.user?.email) {
      return promiseOf<AccountReponse__Output>(callback => this.#client.create({
        name: session.user?.name,
        email: session.user?.email
      }, buildMetadata(session), callback))
    } else {
      throw noEmailAssociated()
    }
  }

  get(session: AuthSession): Promise<Account> {
    if (session.user?.email) {
      return promiseOf<Account__Output>(callback => this.#client.get({ email: session.user?.email }, buildMetadata(session), callback))
    } else {
      throw noEmailAssociated()
    }
  }

  async update(account: AccountUpdate, session: AuthSession): Promise<Account> {
    await promiseOf<AccountReponse__Output>(callback => this.#client.update(account, buildMetadata(session), callback))
    return account
  }
}

export const accountService = new AccountService(accountServiceClient)
