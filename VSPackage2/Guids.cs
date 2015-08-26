// Guids.cs
// MUST match guids.h
using System;

namespace UniversityofWashington.VSPackage2
{
    static class GuidList
    {
        public const string guidVSPackage2PkgString = "f115083d-79a3-42c2-9a8a-906524cc4f92";
        public const string guidVSPackage2CmdSetString = "74080245-4d20-4e93-bc00-617824e8cf07";
        public const string guidToolWindowPersistanceString = "d571123a-b03a-4c5b-962b-9674fdfd060c";

        public static readonly Guid guidVSPackage2CmdSet = new Guid(guidVSPackage2CmdSetString);
    };
}