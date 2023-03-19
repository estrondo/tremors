import * as grpcLibrary from '@grpc/grpc-js'
import * as protoLoader from '@grpc/proto-loader'
import { ProtoGrpcType } from '../../utils/@types/account'


const accountPackageDef = protoLoader.loadSync('./server/utils/grpc/account.proto', {})
const accountPackageObj = (grpcLibrary.loadPackageDefinition(accountPackageDef) as unknown) as ProtoGrpcType

const accountConfig = useRuntimeConfig().grpc.account

export const accountServiceClient = new accountPackageObj
  .grpc.webapi.AccountService(accountConfig.address, grpcLibrary.credentials.createInsecure())