const { defineConfig } = require('@vue/cli-service')
const webpack = require('webpack')

module.exports = defineConfig({
    transpileDependencies: true,

    publicPath: '/',

    devServer: {
        proxy: 'http://localhost:8088',
        // proxy: 'http://89.111.155.206:8080'
        // proxy: 'http://89.111.155.206:8189'
        // proxy: 'http://212.22.70.159:1337/'
        // proxy: 'http://localhost:8080/'
    },


    configureWebpack: {
        plugins: [
            new webpack.DefinePlugin({
                __VUE_PROD_DEVTOOLS__: 'true',
            }),
        ],
    },
})
