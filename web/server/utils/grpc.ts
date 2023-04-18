import * as grpc from '@grpc/grpc-js'
import * as protoLoader from '@grpc/proto-loader'
import { ProtoGrpcType } from './@types/account'
import { AuthSession } from './types'


const accountPackageDef = protoLoader.loadSync('./assets/grpc/account.proto', {})
const accountPackageObj = (grpc.loadPackageDefinition(accountPackageDef) as unknown) as ProtoGrpcType

const accountConfig = useRuntimeConfig().grpc.account

export function buildMetadataFromAuthSession(session: AuthSession): grpc.Metadata {
  const metadata = new grpc.Metadata()

  if (session && session.token?.id) {
    metadata.set('authorization', `Bearer ${session.token?.id}`)
  }

  return metadata
}

function createCredentials(useSSL: boolean) {
  return useSSL ? grpc.credentials.createSsl() : grpc.credentials.createInsecure()
}

export const accountServiceClient = new accountPackageObj
  .grpc.webapi.AccountService(accountConfig.address, createCredentials(accountConfig.ssl))